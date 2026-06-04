package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lógica de negocio para productos.
 *
 * Principios aplicados:
 *  - SRP: sólo orquesta la lógica de Producto.
 *  - DIP: depende de abstracciones (interfaces del repositorio), no de implementaciones.
 *  - @Transactional(readOnly=true) en lectura para optimizar el contexto de JPA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ─── Consultas (solo lectura) ──────────────────────────────────────────────

    /**
     * Retorna una página de productos activos, opcionalmente filtrados por categoría.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return productRepository
                    .findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                    .map(this::toDTO);
        }
        return productRepository.findByIsActiveTrue(pageable).map(this::toDTO);
    }

    /**
     * Busca un producto por ID. Lanza excepción si no existe o está inactivo.
     */
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id));
        return toDTO(product);
    }

    // ─── Comandos (escritura) ──────────────────────────────────────────────────

    /**
     * Crea y persiste un nuevo producto.
     * Valida que la categoría exista antes de asociarla.
     */
    @Transactional
    public ProductDTO create(ProductDTO dto) {
        log.info("Creando producto: {}", dto.getName());

        Category category = resolveCategory(dto.getCategoryId());
        Product product = toEntity(dto, category);
        product.setIsActive(true);

        Product saved = productRepository.save(product);
        log.info("Producto creado con id={}", saved.getId());
        return toDTO(saved);
    }

    /**
     * Actualiza un producto existente.
     * Solo modifica los campos permitidos (no cambia el stock directamente desde aquí —
     * eso lo maneja OrderService al confirmar un pago).
     */
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        log.info("Actualizando producto id={}", id);

        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id));

        Category category = resolveCategory(dto.getCategoryId());

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);

        return toDTO(productRepository.save(product));
    }

    /**
     * Soft-delete: marca el producto como inactivo en lugar de eliminarlo.
     * Preserva la integridad de los OrderItems históricos.
     */
    @Transactional
    public void delete(Long id) {
        log.warn("Soft-delete sobre producto id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id));

        product.setIsActive(false);
        productRepository.save(product);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + categoryId));
    }

    /**
     * Mapeo de entidad → DTO.
     * En proyectos mayores considera MapStruct para evitar código manual.
     */
    private ProductDTO toDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .build();
    }

    /** Mapeo de DTO → entidad (para creación). */
    private Product toEntity(ProductDTO dto, Category category) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .imageUrl(dto.getImageUrl())
                .category(category)
                .build();
    }
}
