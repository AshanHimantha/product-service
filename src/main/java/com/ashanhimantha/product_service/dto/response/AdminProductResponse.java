package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import com.ashanhimantha.product_service.entity.enums.ProductType;
import lombok.Data;
import java.time.Instant;

import java.util.List;
/**
 * A detailed DTO for internal use (Admins, Suppliers).
 * It includes sensitive business data like the unitCost.
 */
@Data
public class AdminProductResponse {
    private Long id;
    private String name;
    private String description;
    private ProductType productType;
    private ProductStatus status;
    private CategoryResponse category;
    private List<VariantResponse> variants; // Detailed variant info with costs and prices
    private Integer totalStock; // Total stock across all variants
    private List<String> imageUrls; // Product images
    private Instant createdAt;
    private Instant updatedAt;
}