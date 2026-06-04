package com.example.demo.service;

// Cambia esto:
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────────
 *  MercadoPagoService
 * ─────────────────────────────────────────────────────────────────
 * Orquesta el flujo completo de pagos con Mercado Pago:
 *
 *  1. createPreference()  — el backend crea una "preferencia" de pago
 *     y devuelve el init_point (URL de checkout) al frontend.
 *
 *  2. processWebhook()    — cuando MP llama al webhook, verificamos
 *     el pago, actualizamos la orden y disparamos el evento a n8n.
 *
 * Documentación MP: https://www.mercadopago.com.ar/developers/es/docs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private final OrderRepository orderRepository;
    private final N8nWebhookService n8nWebhookService;
    private final RestTemplate restTemplate;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.webhook-secret}")
    private String webhookSecret;

    @Value("${app.base-url}")
    private String appBaseUrl;

    private static final String MP_API_BASE = "https://api.mercadopago.com";

    /**
     * Crea una preferencia de pago en MP a partir de una Order.
     *
     * Flujo:
     *  Order (DB) ──► buildPreferencePayload() ──► POST /v1/checkout/preferences
     *              ──► { id, init_point } ──► guardamos el preferenceId en la Order
     *
     * @param orderId ID de la orden creada previamente
     * @return URL de Checkout Pro para redirigir al usuario
     */
    @Transactional
    public String createPreference(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + orderId));

        Map<String, Object> payload = buildPreferencePayload(order);

        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                MP_API_BASE + "/checkout/preferences", request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Error al crear preferencia en Mercado Pago");
        }

        String preferenceId = (String) response.getBody().get("id");
        String initPoint    = (String) response.getBody().get("init_point");

        // Persiste el preferenceId para relacionarlo con el webhook entrante
        order.setMpPreferenceId(preferenceId);
        orderRepository.save(order);

        log.info("Preferencia MP creada para orden={} preferenceId={}", orderId, preferenceId);
        return initPoint;
    }

    /**
     * Procesa el webhook de Mercado Pago (notificación IPN/Webhooks v2).
     *
     * MP enviará un POST a /api/v1/payments/webhook con:
     *   { "type": "payment", "data": { "id": "12345678" } }
     *
     * Pasos:
     *  1. Verificar que el payment.status sea "approved".
     *  2. Buscar la orden asociada por external_reference.
     *  3. Actualizar estado → PAID.
     *  4. Descontar stock de cada item.
     *  5. Disparar evento a n8n.
     */
    @Transactional
    public void processWebhook(String paymentId) {
        log.info("Procesando webhook MP para paymentId={}", paymentId);

        // 1. Consultar el pago en la API de MP
        Map<String, Object> payment = getPaymentDetails(paymentId);
        String status = (String) payment.get("status");

        if (!"approved".equals(status)) {
            log.info("Pago {} con estado '{}', no se procesa.", paymentId, status);
            return;
        }

        // 2. Recuperar la orden por external_reference (que guardamos como orderId)
        String externalReference = (String) payment.get("external_reference");
        Long orderId = Long.parseLong(externalReference);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Orden {} ya estaba pagada, ignorando webhook duplicado.", orderId);
            return;
        }

        // 3. Actualizar orden
        order.setStatus(OrderStatus.PAID);
        order.setMpPaymentId(paymentId);
        order.setPaidAt(LocalDateTime.now());

        // 4. Descontar stock
        order.getItems().forEach(item -> {
            item.getProduct().setStock(
                    item.getProduct().getStock() - item.getQuantity()
            );
        });

        orderRepository.save(order);
        log.info("Orden {} marcada como PAID.", orderId);

        // 5. Notificar a n8n de forma asíncrona
        n8nWebhookService.notifyOrderPaid(order);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private Map<String, Object> buildPreferencePayload(Order order) {
        List<Map<String, Object>> items = new ArrayList<>();

        order.getItems().forEach(item -> {
            Map<String, Object> mpItem = new HashMap<>();
            mpItem.put("title",       item.getProduct().getName());
            mpItem.put("quantity",    item.getQuantity());
            mpItem.put("unit_price",  item.getUnitPrice().doubleValue());
            mpItem.put("currency_id", "ARS"); // Cambiar según país
            items.add(mpItem);
        });

        Map<String, Object> backUrls = new HashMap<>();
        backUrls.put("success", appBaseUrl + "/checkout/success");
        backUrls.put("failure", appBaseUrl + "/checkout/failure");
        backUrls.put("pending", appBaseUrl + "/checkout/pending");

        Map<String, Object> payload = new HashMap<>();
        payload.put("items",              items);
        payload.put("back_urls",          backUrls);
        payload.put("auto_return",        "approved");
        payload.put("external_reference", order.getId().toString());
        payload.put("notification_url",   appBaseUrl + "/api/v1/payments/webhook");

        return payload;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getPaymentDetails(String paymentId) {
        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                MP_API_BASE + "/v1/payments/" + paymentId,
                HttpMethod.GET, request, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("No se pudo obtener el pago " + paymentId + " de MP");
        }
        return response.getBody();
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}


// ─────────────────────────────────────────────────────────────────────────────
//  Archivo separado: N8nWebhookService.java
// ─────────────────────────────────────────────────────────────────────────────

//package com.hydroswell.api.service;
//
//import com.hydroswell.api.model.Order;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Servicio que dispara un webhook hacia n8n cuando una orden es pagada.
// *
// * La llamada es @Async para no bloquear el hilo de la transacción principal.
// * Configura el webhook URL en application.yml:
// *   n8n.webhook-url: https://tu-n8n.dominio.com/webhook/order-paid
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class N8nWebhookService {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${n8n.webhook-url}")
//    private String n8nWebhookUrl;
//
//    /**
//     * Envía los datos de la orden pagada a n8n.
//     * @Async requiere @EnableAsync en la clase de configuración principal.
//     *
//     * El payload puede usarse en n8n para:
//     *  - Enviar email de confirmación al cliente.
//     *  - Actualizar un Google Sheet de ventas.
//     *  - Notificar al equipo por Slack/Discord.
//     *  - Crear la guía de envío en el sistema logístico.
//     */
//    @Async
//    public void notifyOrderPaid(Order order) {
//        try {
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("event",         "ORDER_PAID");
//            payload.put("orderId",        order.getId());
//            payload.put("customerEmail",  order.getUser().getEmail());
//            payload.put("customerName",   order.getUser().getFullName());
//            payload.put("totalAmount",    order.getTotalAmount());
//            payload.put("paidAt",         order.getPaidAt().toString());
//            payload.put("shippingAddress",order.getShippingAddress());
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
//            ResponseEntity<String> response = restTemplate.postForEntity(n8nWebhookUrl, request, String.class);
//
//            log.info("n8n notificado para orden {}. Status: {}", order.getId(), response.getStatusCode());
//
//        } catch (Exception e) {
//            // No propagamos el error — el pago ya fue procesado correctamente.
//            // Considera una tabla de reintentos (outbox pattern) para producción.
//            log.error("Error al notificar a n8n para orden {}: {}", order.getId(), e.getMessage());
//        }
//    }
//}
