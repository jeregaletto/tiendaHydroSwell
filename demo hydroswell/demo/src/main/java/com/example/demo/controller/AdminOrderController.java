package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    // Obtener absolutamente todas las órdenes de la base de datos
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        // Podés usar findAll o armar una query ordenada por fecha en tu repository
        return ResponseEntity.ok(orderRepository.findAll());
    }
}