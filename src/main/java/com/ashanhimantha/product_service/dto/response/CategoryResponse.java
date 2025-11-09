package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Category information")
public class CategoryResponse {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Vegetables")
    private String name;

    @Schema(description = "Category description", example = "Fresh organic vegetables")
    private String description;

    @Schema(description = "Category image URL", example = "https://s3.amazonaws.com/bucket/category.jpg")
    private String imageUrl;

    @Schema(description = "Category type information")
    private CategoryTypeResponse categoryType;

    @Schema(description = "Category status", example = "ACTIVE")
    private Status status;
}
