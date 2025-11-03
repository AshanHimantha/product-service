package com.ashanhimantha.product_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a category type template with sizing/measurement options
 * Example: "Clothing Sizes" with options [S, M, L, XL]
 */
@Entity
@Table(name = "category_types")
@Data
@SQLDelete(sql = "UPDATE category_types SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class CategoryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Clothing Sizes - Letter", "Pants Sizes", "Shoe Sizes"

    /**
     * Available size/measurement options for this type
     * Stored as comma-separated values
     * Example: "S,M,L,XL,XXL" or "28,30,32,34,36" or "7,8,9,10,11,12"
     */
    @Column(nullable = false, length = 500)
    private String sizeOptions;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * Get size options as a list
     */
    public List<String> getSizeOptionsAsList() {
        if (sizeOptions == null || sizeOptions.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(sizeOptions.split(","));
    }

    /**
     * Set size options from a list
     */
    public void setSizeOptionsFromList(List<String> options) {
        if (options == null || options.isEmpty()) {
            this.sizeOptions = "";
        } else {
            this.sizeOptions = String.join(",", options);
        }
    }
}
