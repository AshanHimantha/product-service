package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import com.ashanhimantha.product_service.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.Instant;

import java.util.List;
/**
 * A detailed DTO for internal use (Admins, Suppliers).
 * It includes sensitive business data like the unitCost.
 */
@Data
@Schema(description = "Detailed product information for administrators including costs and status")
public class AdminProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Organic Tomatoes")
    private String name;

    @Schema(description = "Product description", example = "Fresh organic tomatoes from local farms")
    private String description;

    @Schema(description = "Product type - STOCK (has inventory) or NON_STOCK (made-to-order)", example = "STOCK")
    private ProductType productType;

    @Schema(description = "Product status - ACTIVE or INACTIVE", example = "ACTIVE")
    private Status status;

    @Schema(description = "Category information")
    private CategoryResponse category;

    @Schema(description = "Detailed variant information including costs and prices")
    private List<VariantResponse> variants;

    @Schema(description = "Total stock across all variants", example = "150")
    private Integer totalStock;

    @Schema(description = "Product image URLs", example = "[\"https://s3.amazonaws.com/bucket/image1.jpg\", \"https://s3.amazonaws.com/bucket/image2.jpg\"]")
    private List<String> imageUrls;

    @Schema(description = "Product creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Product last update timestamp", example = "2024-11-08T14:20:00Z")
    private Instant updatedAt;
}