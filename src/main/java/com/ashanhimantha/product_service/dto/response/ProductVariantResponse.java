package com.ashanhimantha.product_service.dto.response;

import lombok.Data;

import java.time.Instant;

/**
 * DTO for Product Variant responses
 */
@Data
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String color;
    private String size;
    private Double unitCost;
    private Double sellingPrice;
    private Integer quantity;
    private String sku;
    private Boolean isActive;
    private String variantName;
    private Instant createdAt;
    private Instant updatedAt;
}

