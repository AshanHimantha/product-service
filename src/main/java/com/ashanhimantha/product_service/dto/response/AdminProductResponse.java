package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import lombok.Data;
import java.time.Instant;

/**
 * A detailed DTO for internal use (Admins, Suppliers).
 * It includes sensitive business data like the unitCost.
 */
@Data
public class AdminProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double unitCost; // <-- SENSITIVE DATA
    private Double sellingPrice;
    private String producerInfo;
    private Integer stockCount;
    private String supplierId;
    private ProductStatus status;
    private CategoryResponse category;
    private Instant createdAt;
    private Instant updatedAt;
    private String imageUrl;
}