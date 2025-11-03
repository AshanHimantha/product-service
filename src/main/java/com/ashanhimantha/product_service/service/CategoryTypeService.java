package com.ashanhimantha.product_service.service;

import com.ashanhimantha.product_service.dto.request.CategoryTypeRequest;
import com.ashanhimantha.product_service.entity.CategoryType;

import java.util.List;

public interface CategoryTypeService {
    CategoryType createCategoryType(CategoryTypeRequest request);
    CategoryType getCategoryTypeById(Long id);
    List<CategoryType> getAllCategoryTypes();
    CategoryType updateCategoryType(Long id, CategoryTypeRequest request);
    void deleteCategoryType(Long id);
}
