package com.ashanhimantha.product_service.entity;

import com.ashanhimantha.product_service.entity.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
public class CategoryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Clothing Sizes - Letter", "Pants Sizes", "Shoe Sizes"

    @Column(nullable = false, length = 500)
    private String sizeOptions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

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
