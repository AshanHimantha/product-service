package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import com.ashanhimantha.product_service.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Schema(description = "Product information with variants and stock details")
public class ProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Organic Tomatoes")
    private String name;

    @Schema(description = "Product description", example = "Fresh organic tomatoes from local farms")
    private String description;

    @Schema(description = "Product type - STOCK or NON_STOCK", example = "STOCK")
    private ProductType productType;

    @Schema(description = "Product status - ACTIVE or INACTIVE", example = "ACTIVE")
    private Status status;

    @Schema(description = "Category information")
    private CategoryResponse category;

    @Schema(description = "Product creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Product last update timestamp", example = "2024-11-08T14:20:00Z")
    private Instant updatedAt;

    @Schema(description = "Available product variants with stock info and prices")
    private List<VariantResponse> variants;

    @Schema(description = "Total stock across all variants", example = "150")
    private Integer totalStock;

    @Schema(description = "Product image URLs", example = "[\"https://s3.amazonaws.com/bucket/image1.jpg\"]")
    private java.util.List<String> imageUrls;

}