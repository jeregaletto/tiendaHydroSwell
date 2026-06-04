package com.example.demo.controller;

import com.example.demo.service.MercadoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class WebhookController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacionMercadoPago(
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "type", required = false) String type,
            @RequestBody(required = false) Map<String, Object> body) {

        // Mercado Pago avisa de muchas cosas, a nosotros solo nos importan los pagos ("payment")
        String paymentId = null;

        // MP lo puede mandar por Query Params o dentro del Body JSON según la versión
        if ("payment".equals(type) && dataId != null) {
            paymentId = dataId;
        } else if (body != null && "payment".equals(body.get("type"))) {
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data != null) {
                paymentId = (String) data.get("id");
            }
        }

        if (paymentId != null) {
            // Se ejecuta tu proceso transaccional de stock y aviso a n8n
            mercadoPagoService.processWebhook(paymentId);
        }

        // MP exige que siempre respondamos 200 OK rápido para no reintentar el envío
        return ResponseEntity.ok().build();
    }
}