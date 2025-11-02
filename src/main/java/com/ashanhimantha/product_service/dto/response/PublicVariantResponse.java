package com.ashanhimantha.product_service.dto.response;

import lombok.Data;

/**
 * Public-facing response DTO for product variants
 * Shows only essential customer-facing information
 */
@Data
public class PublicVariantResponse {
    private Long id; // Needed for cart operations
    private String color;
    private String size;
    private Double price;
    private Integer availableStock; // null for NON_STOCK type products
}
