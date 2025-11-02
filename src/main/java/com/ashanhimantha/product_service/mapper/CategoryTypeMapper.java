package com.ashanhimantha.product_service.mapper;

import com.ashanhimantha.product_service.dto.response.CategoryTypeResponse;
import com.ashanhimantha.product_service.entity.CategoryType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryTypeMapper {

    public CategoryTypeResponse toResponse(CategoryType categoryType) {
        if (categoryType == null) {
            return null;
        }

        CategoryTypeResponse response = new CategoryTypeResponse();
        response.setId(categoryType.getId());
        response.setName(categoryType.getName());
        response.setSizingType(categoryType.getSizingType());
        response.setSizeOptions(categoryType.getSizeOptionsAsList()); // Only send as list

        return response;
    }

    public List<CategoryTypeResponse> toResponseList(List<CategoryType> categoryTypes) {
        return categoryTypes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}

