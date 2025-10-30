package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.CategoryRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController extends AbstractController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategoriesAsList();
        return success("Categories retrieved successfully", categories);
    }


    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return success("Category retrieved successfully", category);
    }

    @PostMapping
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        Category createdCategory = categoryService.createCategory(categoryRequest);
        return created("Category created successfully", createdCategory);
    }

    @PutMapping("/{categoryId}") // ADD THIS ENDPOINT
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CategoryRequest categoryRequest) {
        Category updatedCategory = categoryService.updateCategory(categoryId, categoryRequest);
        return success("Category updated successfully", updatedCategory);
    }

    @DeleteMapping("/{categoryId}") // ADD THIS ENDPOINT
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return success("Category deleted successfully", null);
    }
}