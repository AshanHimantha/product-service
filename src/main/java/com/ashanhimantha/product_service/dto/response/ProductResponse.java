package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import lombok.Data;

import java.time.Instant;

/**
 * DTO for API responses containing Product information.
 * This object is a safe representation of a Product, including its nested Category details.
 */
@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
//    private Double unitCost;
    private Double sellingPrice;
    private String producerInfo;
    private Integer stockCount;
//    private String supplierId;
    private ProductStatus status;
    private CategoryResponse category; // Embeds the clean Category DTO
    private Instant createdAt;
    private Instant updatedAt;

}