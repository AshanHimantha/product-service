package com.ashanhimantha.product_service.service;

import com.ashanhimantha.product_service.dto.request.CategoryRequest;
import com.ashanhimantha.product_service.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CategoryService {
    Category createCategory(CategoryRequest categoryRequest);
    Category getCategoryById(Long categoryId);
    Page<Category> getAllCategories(Pageable pageable);
    List<Category> getAllCategoriesAsList();
    Category updateCategory(Long categoryId, CategoryRequest categoryRequest);
    void deleteCategory(Long categoryId);
    void updateCategoryImage(Long categoryId, String imageUrl);
    boolean categoryHasProducts(Long categoryId);
}