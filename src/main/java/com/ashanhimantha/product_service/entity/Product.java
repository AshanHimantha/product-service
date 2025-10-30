package com.ashanhimantha.product_service.entity;


import com.ashanhimantha.product_service.entity.enums.ProductStatus;
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


    @Column(nullable = false)
    private Double unitCost; // The price the business paid for one unit of the product.

    @Column(nullable = false)
    private Double sellingPrice; // The price the product is sold to the customer for.


    private String producerInfo;


    @Column(nullable = false)
    private Integer stockCount;


    @Column(nullable = false, updatable = false)
    private String supplierId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    // New field to store the public S3 URL (or object key) for the product image
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}