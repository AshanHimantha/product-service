package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Public-facing response DTO for products
 * Excludes internal business details like status, timestamps, and detailed stock info
 */
@Data
@Schema(description = "Public product information visible to all users")
public class PublicProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Organic Tomatoes")
    private String name;

    @Schema(description = "Product description", example = "Fresh organic tomatoes from local farms")
    private String description;

    @Schema(description = "Product type - STOCK (has inventory) or NON_STOCK (made-to-order)", example = "STOCK")
    private ProductType productType;

    @Schema(description = "Category information")
    private CategoryResponse category;

    @Schema(description = "Available product variants with pricing")
    private List<PublicVariantResponse> variants;

    @Schema(description = "Product image URLs", example = "[\"https://s3.amazonaws.com/bucket/image1.jpg\", \"https://s3.amazonaws.com/bucket/image2.jpg\"]")
    private List<String> imageUrls;
}
