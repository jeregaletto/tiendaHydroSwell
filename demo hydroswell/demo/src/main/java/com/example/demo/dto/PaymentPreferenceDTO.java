package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

/**
 * DTO para el endpoint POST /api/v1/payments/preference/{orderId}
 *
 * Request: el frontend envía el orderId (puede ir en el path o en el body).
 * Response: el backend devuelve la URL de Checkout Pro de Mercado Pago.
 *
 * Flujo completo:
 *  1. Frontend llama a POST /api/v1/payments/preference/{orderId}
 *  2. Backend llama a MercadoPagoService.createPreference(orderId)
 *  3. MP devuelve { id, init_point }
 *  4. Backend retorna PaymentPreferenceDTO con checkoutUrl
 *  5. Frontend redirige al usuario a checkoutUrl
 *  6. MP llama al webhook /api/v1/payments/webhook cuando el pago es procesado
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentPreferenceDTO {

    // ─── Request ─────────────────────────────────────────────────────────────

    @NotNull(message = "El ID de la orden es obligatorio")
    @Positive(message = "El ID de la orden debe ser positivo")
    private Long orderId;

    // ─── Response ────────────────────────────────────────────────────────────

    /** ID de la preferencia generado por Mercado Pago. */
    private String preferenceId;

    /**
     * URL de Checkout Pro a la que se redirige al usuario.
     * En sandbox: https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=...
     * En producción: https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=...
     */
    private String checkoutUrl;

    /** Monto total de la orden (informativo, para mostrar en UI). */
    private java.math.BigDecimal totalAmount;

    // ─── Factory methods ─────────────────────────────────────────────────────

    /** Crea la respuesta exitosa que se devuelve al frontend. */
    public static PaymentPreferenceDTO success(String preferenceId,
                                               String checkoutUrl,
                                               java.math.BigDecimal totalAmount) {
        return PaymentPreferenceDTO.builder()
                .preferenceId(preferenceId)
                .checkoutUrl(checkoutUrl)
                .totalAmount(totalAmount)
                .build();
    }
}
