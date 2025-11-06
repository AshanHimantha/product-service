package com.ashanhimantha.product_service.dto.request;

import com.ashanhimantha.product_service.entity.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

@NotNull(message = "Category Type is required")
private Long categoryTypeId; // Optional: Link to a CategoryType for sizing options

    private Status status;
}