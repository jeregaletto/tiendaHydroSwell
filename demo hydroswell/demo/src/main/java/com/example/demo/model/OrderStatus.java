package com.example.demo.model;

/**
 * Estados posibles de una orden.
 * Se almacena como String en la BD (EnumType.STRING) para legibilidad
 * y resistencia a reordenamientos del enum.
 *
 * Ubicado en el paquete model para cohesión — es parte del dominio Order.
 */
public enum OrderStatus {
    /** Orden creada, esperando confirmación de pago. */
    PENDING,

    /** Pago confirmado por Mercado Pago vía webhook. */
    PAID,

    /** Pedido despachado al cliente. */
    SHIPPED,

    /** Cancelado por el usuario, pago rechazado o timeout. */
    CANCELLED
}
