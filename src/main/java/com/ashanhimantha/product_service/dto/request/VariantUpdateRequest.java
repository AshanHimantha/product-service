package com.ashanhimantha.product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO for updating any combination of product variant fields
 * All fields are optional - only provided fields will be updated
 */
@Data
public class VariantUpdateRequest {

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit cost must be greater than 0")
    private Double unitCost;

    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    private Double sellingPrice;

    private Boolean isActive;
}

