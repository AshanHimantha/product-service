package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import lombok.Data;

import java.util.List;

/**
 * Public-facing response DTO for products
 * Excludes internal business details like status, timestamps, and detailed stock info
 */
@Data
public class PublicProductResponse {
    private Long id;
    private String name;
    private String description;
    private ProductType productType; // STOCK or NON_STOCK
    private CategoryResponse category;
    private List<PublicVariantResponse> variants;
    private List<String> imageUrls;
}
