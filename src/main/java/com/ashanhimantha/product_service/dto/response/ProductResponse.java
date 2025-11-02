package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import lombok.Data;

import java.time.Instant;

import java.util.List;
@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private ProductStatus status;
    private CategoryResponse category;
    private Instant createdAt;
    private Instant updatedAt;
    private List<VariantResponse> variants; // Available variants with stock info and prices
    private Integer totalStock; // Total stock across all variants
    private java.util.List<String> imageUrls;

}