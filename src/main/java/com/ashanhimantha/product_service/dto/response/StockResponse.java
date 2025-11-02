package com.ashanhimantha.product_service.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class StockResponse {
    private Long id;
    private Double unitCost; // Cost for simple products
    private Double sellingPrice; // Selling price for simple products
    private Integer quantity;
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private Instant createdAt;
    private Instant updatedAt;
}
