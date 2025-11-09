package com.ashanhimantha.product_service.mapper;

import com.ashanhimantha.product_service.dto.response.CategoryResponse;
import com.ashanhimantha.product_service.dto.response.CategorySummaryResponse;
import com.ashanhimantha.product_service.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryTypeMapper.class})
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    CategorySummaryResponse toSummaryResponse(Category category);

    List<CategorySummaryResponse> toSummaryResponseList(List<Category> categories);
}
