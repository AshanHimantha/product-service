package com.ashanhimantha.product_service.dto.request;

import com.ashanhimantha.product_service.entity.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CategoryTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotEmpty(message = "Size options are required")
    private List<String> sizeOptions; // e.g., ["S", "M", "L", "XL"] or ["28", "30", "32"]

    @NotNull(message = "Status is required")
    private Status status;

}
