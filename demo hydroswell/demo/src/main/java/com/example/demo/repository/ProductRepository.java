package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Product;

/**
 * Repositorio para Product.
 *
 * Spring Data JPA genera la implementación en tiempo de ejecución
 * a partir de los nombres de los métodos (query derivation).
 *
 * Los métodos con IsActiveTrue filtran automáticamente los soft-deleted.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Todos los productos activos, paginados. */
    Page<Product> findByIsActiveTrue(Pageable pageable);

    /** Productos activos filtrados por categoría, paginados. */
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    /** Busca un producto activo por ID (evita devolver soft-deleted). */
    Optional<Product> findByIdAndIsActiveTrue(Long id);

    /**
     * Búsqueda de texto por nombre o descripción (útil para un buscador).
     * ILIKE = case-insensitive en PostgreSQL.
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchByText(@Param("query") String query, Pageable pageable);

    /** Verifica si hay stock suficiente (útil para validaciones rápidas). */
    @Query("SELECT p.stock FROM Product p WHERE p.id = :id")
    Optional<Integer> findStockById(@Param("id") Long id);
}
