package com.ashanhimantha.product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/**
 * DTO for updating the pricing of a product variant
 * At least one field must be provided
 */
@Data
public class VariantPricingUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit cost must be greater than 0")
    private Double unitCost;

    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    private Double sellingPrice;
}

