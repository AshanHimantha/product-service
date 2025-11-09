package com.ashanhimantha.product_service.mapper;

import com.ashanhimantha.product_service.dto.response.CategoryTypeResponse;
import com.ashanhimantha.product_service.entity.CategoryType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryTypeMapper {

    @Mapping(target = "sizeOptions", expression = "java(categoryType.getSizeOptionsAsList())")
    CategoryTypeResponse toResponse(CategoryType categoryType);

    List<CategoryTypeResponse> toResponseList(List<CategoryType> categoryTypes);
}
