package com.ashanhimantha.product_service.dto.request;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import com.ashanhimantha.product_service.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Product creation/update request")
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    @Schema(description = "Product name", example = "Organic Tomatoes", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Product description", example = "Fresh organic tomatoes from local farms")
    private String description;

    @NotNull(message = "Product type is required")
    @Schema(description = "Product type - STOCK (has inventory) or NON_STOCK (made-to-order)", example = "STOCK", requiredMode = Schema.RequiredMode.REQUIRED)
    private ProductType productType = ProductType.STOCK;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Category ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "Available colors for the product (optional). Leave empty if product doesn't have color variants", example = "[\"Red\", \"Blue\", \"Green\"]")
    private List<String> colors;

    @Schema(description = "Product variants with sizes and pricing information")
    private List<VariantRequest> variants;

    @Schema(description = "Product status (ACTIVE, INACTIVE)", example = "ACTIVE")
    private Status status;

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    public boolean hasColors() {
        return colors != null && !colors.isEmpty();
    }
}
