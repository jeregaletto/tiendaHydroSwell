package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * Representa una línea de producto dentro de una orden.
 *
 * Importante: unitPrice y subtotal se snapshot al momento de la compra.
 * Esto significa que si el precio del producto cambia después, el historial
 * de órdenes sigue siendo fiel al precio que pagó el cliente.
 *
 * No usamos @Data de Lombok en entidades JPA con relaciones bidireccionales
 * para evitar el StackOverflow que genera el toString/equals generado
 * al ciclar entre Order ↔ OrderItem.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    // EAGER: casi siempre necesitamos el nombre del producto en el item
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    /** Precio unitario al momento de la compra (snapshot). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** subtotal = quantity × unitPrice. Se calcula y persiste por conveniencia. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Factory method que crea un OrderItem y calcula el subtotal automáticamente.
     * Uso: OrderItem.of(product, 3)
     */
    public static OrderItem of(Product product, int quantity) {
        BigDecimal unitPrice = product.getPrice();
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }
}
