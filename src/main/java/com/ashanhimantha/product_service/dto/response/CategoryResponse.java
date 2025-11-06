package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private CategoryTypeResponse categoryType; // Nested clean response
    private Status status;
}
