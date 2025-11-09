package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Category type with sizing options")
public class CategoryTypeResponse {

    @Schema(description = "Category type ID", example = "1")
    private Long id;

    @Schema(description = "Category type name", example = "Weight-based")
    private String name;

    @Schema(description = "Available size options", example = "[\"500g\", \"1kg\", \"2kg\"]")
    private List<String> sizeOptions;

    @Schema(description = "Category type status", example = "ACTIVE")
    private Status status;
}
