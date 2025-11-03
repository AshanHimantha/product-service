package com.ashanhimantha.product_service.mapper;

import com.ashanhimantha.product_service.dto.response.CategoryResponse;
import com.ashanhimantha.product_service.dto.response.CategorySummaryResponse;
import com.ashanhimantha.product_service.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final CategoryTypeMapper categoryTypeMapper;

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());

        // Map category type if exists
        if (category.getCategoryType() != null) {
            response.setCategoryType(categoryTypeMapper.toResponse(category.getCategoryType()));
        }

        return response;
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategorySummaryResponse toSummaryResponse(Category category) {
        if (category == null) {
            return null;
        }

        CategorySummaryResponse response = new CategorySummaryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setImageUrl(category.getImageUrl());

        return response;
    }

    public List<CategorySummaryResponse> toSummaryResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }
}
