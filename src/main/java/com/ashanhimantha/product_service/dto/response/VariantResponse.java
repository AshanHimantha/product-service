package com.ashanhimantha.product_service.dto.response;

import lombok.Data;

import java.time.Instant;

/**
 * Response DTO for product variant information
 */
@Data
public class VariantResponse {
    private Long id;
    private String color;
    private String size;
    private Double unitCost;
    private Double sellingPrice;
    private Integer quantity;
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private String sku;
    private Boolean isActive;
    private Boolean isInStock;
    private Boolean needsReorder;
    private Instant createdAt;
    private Instant updatedAt;
}

