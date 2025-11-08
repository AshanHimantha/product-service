package com.ashanhimantha.product_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating a product variant with specific color, size, pricing, and stock
 */
@Data
public class VariantRequest {

    @Size(min = 2, max = 50, message = "Color must be between 2 and 50 characters")
    private String color; // Optional - can be null for products without color variants

    @NotBlank(message = "Size is required")
    @Size(min = 1, max = 10, message = "Size must be between 1 and 10 characters")
    private String size; // e.g., "S", "M", "L", "XL"

    @NotNull(message = "Unit cost is required")
    @PositiveOrZero(message = "Unit cost must be zero or positive")
    @Digits(integer = 8, fraction = 2)
    private Double unitCost;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    @Digits(integer = 8, fraction = 2)
    private Double sellingPrice;

    @NotNull(message = "Initial quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Integer quantity;

    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    private String sku; // Optional unique identifier
}
