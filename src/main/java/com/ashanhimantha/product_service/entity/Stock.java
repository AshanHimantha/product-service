package com.ashanhimantha.product_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "stocks")
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    @Column(nullable = false)
    private Double unitCost; // Cost for simple products without variants

    @Column(nullable = false)
    private Double sellingPrice; // Selling price for simple products without variants

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer reorderLevel = 0; // Minimum stock level before reordering

    @Column(nullable = false)
    private Integer reorderQuantity = 0; // How many to reorder when stock is low

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
