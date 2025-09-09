package com.ashanhimantha.product_service.dto.request;

import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProductStatusRequest {

    @NotNull(message = "New status is required")
    private ProductStatus newStatus;
}