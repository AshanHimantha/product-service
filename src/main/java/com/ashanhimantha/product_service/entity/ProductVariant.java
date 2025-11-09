package com.ashanhimantha.product_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Represents a specific variant of a product (e.g., Red T-Shirt in Size M)
 * Each variant has its own stock, cost, and selling price
 */
@Entity
@Table(name = "product_variants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "color", "size"})
})
@Data
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 50, nullable = true)
    private String color; // null = no color (treated as "NONE")

    @Column(nullable = false)
    private String size; // e.g., "S", "M", "L", "XL"

    @Column(nullable = false)
    private Double unitCost; // Cost for this specific variant

    @Column(nullable = false)
    private Double sellingPrice; // Selling price for this specific variant

    @Column(nullable = true, columnDefinition = "INT DEFAULT 0")
    private Integer quantity = 0; // Stock quantity for this variant

    @Column(length = 50)
    private String sku; // Stock Keeping Unit - unique identifier for this variant

    @Column(nullable = false)
    private Boolean isActive = true; // Can be used to disable specific variants

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * Generate a display name for this variant
     */
    public String getVariantName() {
        if (color != null && !color.isBlank()) {
            return String.format("%s - %s", color, size);
        }
        return size;
    }

}
