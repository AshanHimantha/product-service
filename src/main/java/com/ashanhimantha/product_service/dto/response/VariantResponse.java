package com.ashanhimantha.product_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

/**
 * Response DTO for product variant information
 */
@Data
@Schema(description = "Product variant with detailed pricing and stock information")
public class VariantResponse {

    @Schema(description = "Variant ID", example = "1")
    private Long id;

    @Schema(description = "Variant color", example = "Red")
    private String color;

    @Schema(description = "Variant size", example = "1kg")
    private String size;

    @Schema(description = "Unit cost price", example = "25.50")
    private Double unitCost;

    @Schema(description = "Selling price", example = "35.00")
    private Double sellingPrice;

    @Schema(description = "Available quantity", example = "100")
    private Integer quantity;

    @Schema(description = "Stock keeping unit", example = "TOM-RED-1KG")
    private String sku;

    @Schema(description = "Whether variant is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether variant is in stock", example = "true")
    private Boolean isInStock;

    @Schema(description = "Variant creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Variant last update timestamp", example = "2024-11-08T14:20:00Z")
    private Instant updatedAt;
}
