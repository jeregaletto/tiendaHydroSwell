package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;

    // 1. Obtener todos los productos para listarlos en una tabla
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // 2. Crear un nuevo suplemento (Proteínas, Shakers, etc.)
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // 1. Forzamos que el ID sea null para que sea un INSERT limpio
        product.setId(null); 

        // 2. Forzamos que 'is_active' sea TRUE por defecto para cumplir con la BD
        product.setIsActive(true); // O product.setActive(true); según cómo se llame el setter en tu entidad

        // 3. Opcional: Si 'description' o 'imageUrl' llegan vacíos y tu BD se queja, podés asegurar un texto vacío
        if (product.getImageUrl() == null) product.setImageUrl("");
        if (product.getDescription() == null) product.setDescription(""); // Si tenés este campo en la entidad

        // 4. Guardamos en PostgreSQL
        Product nuevoProduct = productRepository.save(product);
        return ResponseEntity.ok(nuevoProduct);
    }
    // 3. Editar un suplemento existente (Modificar precio o actualizar stock)
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();

        product.setName(productDetails.getName());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        // Agregá acá más campos si tu modelo Product los tiene (ej: descripción)

        Product productActualizado = productRepository.save(product);
        return ResponseEntity.ok(productActualizado);
    }

    // 4. Eliminar un producto del catálogo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();

        productRepository.delete(product);
        return ResponseEntity.ok().build();
    }
}