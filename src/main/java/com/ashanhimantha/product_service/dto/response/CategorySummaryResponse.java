package com.ashanhimantha.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySummaryResponse {
    private Long id;
    private String name;
    private String imageUrl;
}

