package com.ashanhimantha.product_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating a product variant with specific color, size, pricing, and stock
 */
@Data
@Schema(description = "Request body for creating or updating a product variant")
public class VariantRequest {

    @Size(min = 2, max = 50, message = "Color must be between 2 and 50 characters")
    @Schema(description = "Variant color (optional for products without color variants)", example = "Red")
    private String color; // Optional - can be null for products without color variants

    @NotBlank(message = "Size is required")
    @Size(min = 1, max = 10, message = "Size must be between 1 and 10 characters")
    @Schema(description = "Variant size", example = "1kg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String size; // e.g., "S", "M", "L", "XL"

    @NotNull(message = "Unit cost is required")
    @PositiveOrZero(message = "Unit cost must be zero or positive")
    @Digits(integer = 8, fraction = 2)
    @Schema(description = "Unit cost price", example = "25.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double unitCost;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    @Digits(integer = 8, fraction = 2)
    @Schema(description = "Selling price", example = "35.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double sellingPrice;

    @NotNull(message = "Initial quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    @Schema(description = "Initial stock quantity", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    @Schema(description = "Stock keeping unit (optional unique identifier)", example = "TOM-RED-1KG")
    private String sku; // Optional unique identifier
}
