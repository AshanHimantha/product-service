package com.ashanhimantha.product_service.dto.response;


import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
}