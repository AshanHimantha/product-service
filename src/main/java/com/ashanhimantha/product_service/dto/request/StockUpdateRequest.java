package com.ashanhimantha.product_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating stock quantity of a product variant
 */
@Data
public class StockUpdateRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private String reason; // Optional: "RESTOCKED", "SOLD", "DAMAGED", "ADJUSTMENT", etc.
}

