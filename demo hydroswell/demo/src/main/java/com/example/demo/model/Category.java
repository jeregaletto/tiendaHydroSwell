package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Categoría de productos (ej: Proteínas, Pre-Entrenos, Hidratación).
 * Relación: una categoría tiene muchos productos (OneToMany).
 */
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Slug URL-friendly, ej: "proteinas", "pre-entrenos" */
    @Column(unique = true, length = 120)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Auto-genera el slug si no fue provisto
        if (this.slug == null && this.name != null) {
            this.slug = this.name.toLowerCase()
                    .replaceAll("[áäà]", "a")
                    .replaceAll("[éëè]", "e")
                    .replaceAll("[íïì]", "i")
                    .replaceAll("[óöò]", "o")
                    .replaceAll("[úüù]", "u")
                    .replaceAll("[ñ]", "n")
                    .replaceAll("[^a-z0-9]", "-")
                    .replaceAll("-+", "-");
        }
    }
}
