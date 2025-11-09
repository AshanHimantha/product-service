package com.ashanhimantha.product_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating the active status of a product variant
 */
@Data
public class VariantStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private Boolean isActive;
}

