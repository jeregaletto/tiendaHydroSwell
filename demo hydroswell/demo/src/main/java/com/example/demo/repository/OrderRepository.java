package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;

/**
 * Repositorio para Order.
 *
 * Las consultas con JOIN FETCH evitan el problema N+1 al cargar
 * órdenes con sus items en una sola query SQL.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Historial de órdenes de un usuario, ordenadas por fecha desc. */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Historial paginado (útil para el panel de admin). */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Todas las órdenes con un estado específico (para admin). */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /** Correlaciona el webhook de MP con la orden local. */
    Optional<Order> findByMpPreferenceId(String mpPreferenceId);

    /** Evita procesar webhooks duplicados. */
    Optional<Order> findByMpPaymentId(String mpPaymentId);

    /**
     * Carga una orden con todos sus items y productos en una sola query.
     * Evita el problema N+1 cuando necesitamos renderizar el detalle completo.
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    /**
     * Igual que el anterior pero restringido al usuario dueño de la orden.
     * Evita que un CUSTOMER vea órdenes de otro usuario.
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithItems(@Param("id") Long id,
                                               @Param("userId") Long userId);
}
