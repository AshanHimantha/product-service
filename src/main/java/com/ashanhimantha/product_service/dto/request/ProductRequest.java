package com.ashanhimantha.product_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating or updating a Product.
 * This object represents the data sent by a client (e.g., a Supplier).
 */
@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot be longer than 2000 characters")
    private String description;

    @NotNull(message = "Unit cost is required")
    @PositiveOrZero(message = "Unit cost must be zero or positive") // Cost could be zero for promotional items
    @Digits(integer = 8, fraction = 2)
    private Double unitCost;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be a positive number")
    @Digits(integer = 8, fraction = 2)
    private Double sellingPrice;

    @Size(max = 255, message = "Producer info cannot be longer than 255 characters")
    private String producerInfo;

    @NotNull(message = "Stock count is required")
    @Min(value = 0, message = "Stock count cannot be negative")
    private Integer stockCount;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}