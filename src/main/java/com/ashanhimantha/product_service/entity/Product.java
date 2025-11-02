package com.ashanhimantha.product_service.entity;


import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import com.ashanhimantha.product_service.entity.enums.ProductType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
@Table(name = "products")
@Data
public class Product {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType = ProductType.STOCK; // Default to STOCK type

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock; // For simple products without variants

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ProductVariant> variants = new java.util.ArrayList<>(); // For products with color/size variants

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * Check if product has variants
     */
    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    /**
     * Get total stock across all variants
     */
    public Integer getTotalStock() {
        if (hasVariants()) {
            return variants.stream()
                    .mapToInt(ProductVariant::getQuantity)
                    .sum();
        }
        return stock != null ? stock.getQuantity() : 0;
    }
}