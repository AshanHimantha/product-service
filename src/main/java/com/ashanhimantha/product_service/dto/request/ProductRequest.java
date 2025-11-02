package com.ashanhimantha.product_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;


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


    @NotNull(message = "Category ID is required")
    private Long categoryId;
}