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

    @Column(nullable = false)
    private String color; // e.g., "Red", "Blue", "Black"

    @Column(nullable = false)
    private String size; // e.g., "S", "M", "L", "XL"

    @Column(nullable = false)
    private Double unitCost; // Cost for this specific variant

    @Column(nullable = false)
    private Double sellingPrice; // Selling price for this specific variant

    @Column(nullable = false)
    private Integer quantity = 0; // Stock quantity for this variant

    @Column(nullable = false)
    private Integer reorderLevel = 10; // Minimum stock before reorder

    @Column(nullable = false)
    private Integer reorderQuantity = 50; // Amount to reorder

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
        return String.format("%s - %s", color, size);
    }

    /**
     * Check if variant is in stock
     */
    public boolean isInStock() {
        return quantity > 0;
    }

    /**
     * Check if variant needs reordering
     */
    public boolean needsReorder() {
        return quantity <= reorderLevel;
    }
}

