package com.ashanhimantha.product_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Public-facing response DTO for product variants
 * Shows only essential customer-facing information
 */
@Data
@Schema(description = "Public product variant with customer-facing information")
public class PublicVariantResponse {

    @Schema(description = "Variant ID (needed for cart operations)", example = "1")
    private Long id;

    @Schema(description = "Variant color", example = "Red")
    private String color;

    @Schema(description = "Variant size", example = "1kg")
    private String size;

    @Schema(description = "Selling price", example = "35.00")
    private Double price;

    @Schema(description = "Available stock (null for NON_STOCK type products)", example = "100")
    private Integer availableStock;
}
