package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que dispara un webhook hacia n8n cuando una orden es pagada.
 *
 * La llamada es @Async para no bloquear el hilo de la transacción principal.
 * Configura el webhook URL en application.yml:
 *   n8n.webhook-url: https://tu-n8n.dominio.com/webhook/order-paid
 *
 * En n8n, el nodo "Webhook" recibirá este payload y puedes encadenarlo con:
 *  - Send Email (Gmail/SMTP) — confirmación al cliente
 *  - Google Sheets — registro de ventas
 *  - Slack/Discord — notificación al equipo
 *  - HTTP Request — crear guía de envío en Andreani/OCA/etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class N8nWebhookService {

    private final RestTemplate restTemplate;

    @Value("${n8n.webhook-url}")
    private String n8nWebhookUrl;

    /**
     * Envía los datos de la orden pagada a n8n de forma asíncrona.
     * @Async requiere @EnableAsync en AppConfig o en la clase principal.
     */
    @Async
    public void notifyOrderPaid(Order order) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("event",          "ORDER_PAID");
            payload.put("orderId",         order.getId());
            payload.put("customerEmail",   order.getUser().getEmail());
            payload.put("customerName",    order.getUser().getFullName());
            payload.put("totalAmount",     order.getTotalAmount());
            payload.put("paidAt",          order.getPaidAt().toString());
            payload.put("shippingAddress", order.getShippingAddress());
            payload.put("mpPaymentId",     order.getMpPaymentId());

            // Items del pedido para generar el detalle en el email
            payload.put("items", order.getItems().stream().map(item -> {
                Map<String, Object> i = new HashMap<>();
                i.put("productName", item.getProduct().getName());
                i.put("quantity",    item.getQuantity());
                i.put("unitPrice",   item.getUnitPrice());
                i.put("subtotal",    item.getSubtotal());
                return i;
            }).toList());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    n8nWebhookUrl, request, String.class);

            log.info("n8n notificado para orden {}. HTTP Status: {}",
                    order.getId(), response.getStatusCode());

        } catch (Exception e) {
            // No relanzamos el error — el pago ya fue procesado exitosamente.
            // Para producción: implementar el Outbox Pattern para garantizar entrega.
            log.error("Error al notificar a n8n para orden {}: {}",
                    order.getId(), e.getMessage());
        }
    }
}
