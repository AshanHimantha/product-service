package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import com.ashanhimantha.product_service.entity.enums.Status;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private ProductType productType;
    private Status status;
    private CategoryResponse category;
    private Instant createdAt;
    private Instant updatedAt;
    private List<VariantResponse> variants; // Available variants with stock info and prices
    private Integer totalStock; // Total stock across all variants
    private java.util.List<String> imageUrls;

}