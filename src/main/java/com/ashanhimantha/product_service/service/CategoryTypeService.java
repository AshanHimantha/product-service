package com.ashanhimantha.product_service.service;

import com.ashanhimantha.product_service.dto.request.CategoryTypeRequest;
import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.entity.enums.SizingType;

import java.util.List;

public interface CategoryTypeService {
    CategoryType createCategoryType(CategoryTypeRequest request);
    CategoryType getCategoryTypeById(Long id);
    List<CategoryType> getAllCategoryTypes();
    List<CategoryType> getCategoryTypesBySizingType(SizingType sizingType);
    CategoryType updateCategoryType(Long id, CategoryTypeRequest request);
    void deleteCategoryType(Long id);
}

