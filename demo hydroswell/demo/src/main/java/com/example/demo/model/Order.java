package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Representa un pedido realizado por un usuario.
 *
 * Ciclo de vida del estado:
 *   PENDING → (webhook MP aprobado) → PAID → (despacho) → SHIPPED
 *                                    ↘ (MP rechazado / timeout) → CANCELLED
 *
 * La relación con OrderItem usa CascadeType.ALL + orphanRemoval=true
 * para que los items se persistan y eliminen junto con la orden.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Carga LAZY: no necesitamos todos los datos del usuario en cada consulta de orden
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * ID de preferencia generado por Mercado Pago al crear el checkout.
     * Se usa para correlacionar la orden con los webhooks entrantes.
     */
    @Column(length = 100)
    private String mpPreferenceId;

    /**
     * ID del pago confirmado por Mercado Pago.
     * Se completa cuando el webhook notifica el pago aprobado.
     */
    @Column(length = 100)
    private String mpPaymentId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 300)
    private String shippingAddress;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** Se establece cuando el webhook confirma el pago. */
    private LocalDateTime paidAt;

    /**
     * Lista de items del pedido.
     * orphanRemoval=true: si se elimina un item de la lista, se borra de la BD.
     * CascadeType.ALL: persistir/actualizar la orden también persiste/actualiza sus items.
     */
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Helper para agregar items manteniendo la relación bidireccional ──────

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
