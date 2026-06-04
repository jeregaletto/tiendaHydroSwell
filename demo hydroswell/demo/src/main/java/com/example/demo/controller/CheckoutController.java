package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.CheckoutRequestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
@CrossOrigin(origins = "http://localhost:3000")
public class CheckoutController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/test-success")
    public ResponseEntity<Map<String, String>> checkoutPruebaExito(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestBody CheckoutRequestDTO request) {

        // 1. Validar usuario
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // 2. Crear la orden directamente en estado PAID (Pagada)
        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(BigDecimal.ZERO)
                .status(com.example.demo.model.OrderStatus.PAID) 
                .createdAt(java.time.LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // 3. Buscar productos en la BD, calcular subtotales y descontar stock
        for (CheckoutRequestDTO.CartItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId()).orElse(null);
            if (product == null) return ResponseEntity.badRequest().build();

            OrderItem orderItem = OrderItem.of(product, itemDTO.getQuantity());
            order.addItem(orderItem);
            
            // Simula el descuento de stock físico
            product.setStock(product.getStock() - itemDTO.getQuantity());
            
            total = total.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(total);
        
        // 4. Guardamos en PostgreSQL
        orderRepository.save(order);

        // 5. IMPORTANTE: Limpiamos la respuesta devolviendo SOLO un mapa plano de strings.
        // Esto evita que Jackson intente serializar la entidad Order con sus relaciones.
        return ResponseEntity.ok(Map.of("checkoutUrl", "http://localhost:3000/perfil/pedidos"));
    }
}