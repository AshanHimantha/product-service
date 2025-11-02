package com.ashanhimantha.product_service.dto.request;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot be longer than 2000 characters")
    private String description;

    @NotNull(message = "Product type is required")
    private ProductType productType = ProductType.STOCK;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // List of variants for products with color/size options
    private List<VariantRequest> variants;

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }
}
