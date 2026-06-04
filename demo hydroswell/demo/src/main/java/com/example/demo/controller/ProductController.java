package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.EmailService;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    
    @Autowired
    private EmailService emailService; // Inyectamos el servicio que creaste

    @GetMapping("/test-email")
    public String probarEmail() {
    // CAMBIÁ ESTO por tu propio correo para ver si te llega
    String miCorreo = "jgalettocosta@gmail.com";    
    
    try {
        emailService.enviarCorreoVerificacion(miCorreo, "TOKEN-PRUEBA-123");
        return "¡Correo enviado con éxito a " + miCorreo + "! Revisá tu bandeja (y el spam).";
    } catch (Exception e) {
        return "Error al enviar el correo: " + e.getMessage();
    }
    }
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {

        Page<ProductDTO> products = productService.findAll(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.create(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {

        ProductDTO updated = productService.update(id, productDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}