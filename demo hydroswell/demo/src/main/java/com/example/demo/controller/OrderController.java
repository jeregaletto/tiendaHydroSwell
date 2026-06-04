package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "http://localhost:3000") // Alineado con tu AuthController
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        
        // 1. Si no mandan el email desde Next.js, rechazamos la petición
        if (userEmail == null || userEmail.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        // 2. Buscamos al usuario usando tu método real findByEmail
        User user = userRepository.findByEmail(userEmail).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        // 3. Traemos las órdenes ordenadas por fecha usando tu OrderRepository nativo
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        // 4. Devolvemos la lista limpia al frontend
        return ResponseEntity.ok(userOrders);
    }
}