package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double sellingPrice;
    private ProductStatus status;
    private CategoryResponse category;
    private Instant createdAt;
    private Instant updatedAt;
    private java.util.List<String> imageUrls;

}