package com.ashanhimantha.product_service.dto.request;

import com.ashanhimantha.product_service.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Product partial update request - all fields are optional")
public class ProductPatchRequest {

    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    @Schema(description = "Product name", example = "Organic Tomatoes")
    private String name;

    @Schema(description = "Product description", example = "Fresh organic tomatoes from local farms")
    private String description;

    @Schema(description = "Product status (ACTIVE, INACTIVE, DRAFT)", example = "ACTIVE")
    private Status status;
}

