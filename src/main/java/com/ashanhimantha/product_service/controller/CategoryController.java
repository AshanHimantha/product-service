package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.CategoryRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.CategoryResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.mapper.CategoryMapper;
import com.ashanhimantha.product_service.service.CategoryService;
import com.ashanhimantha.product_service.service.ImageUploadService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController extends AbstractController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final ImageUploadService imageUploadService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategoriesAsList();
        List<CategoryResponse> response = categoryMapper.toResponseList(categories);
        return success("Categories retrieved successfully", response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        CategoryResponse response = categoryMapper.toResponse(category);
        return success("Category retrieved successfully", response);
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestParam("name") @NotBlank @Size(min = 3, max = 100) String name,
            @RequestParam(value = "description", required = false) @Size(max = 255) String description,
            @RequestParam(value = "categoryTypeId", required = false) Long categoryTypeId,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(name);
        categoryRequest.setDescription(description);
        categoryRequest.setCategoryTypeId(categoryTypeId);

        Category createdCategory = categoryService.createCategory(categoryRequest);

        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadService.uploadCategoryImage(image, createdCategory.getId());
            categoryService.updateCategoryImage(createdCategory.getId(), imageUrl);
            createdCategory = categoryService.getCategoryById(createdCategory.getId());
        }

        CategoryResponse response = categoryMapper.toResponse(createdCategory);
        return created("Category created successfully", response);
    }

    @PutMapping(value = "/{categoryId}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @RequestParam("name") @NotBlank @Size(min = 3, max = 100) String name,
            @RequestParam(value = "description", required = false) @Size(max = 255) String description,
            @RequestParam(value = "categoryTypeId", required = false) Long categoryTypeId,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName(name);
        categoryRequest.setDescription(description);
        categoryRequest.setCategoryTypeId(categoryTypeId);

        Category updatedCategory = categoryService.updateCategory(categoryId, categoryRequest);

        if (image != null && !image.isEmpty()) {
            Category existingCategory = categoryService.getCategoryById(categoryId);
            if (existingCategory.getImageUrl() != null && !existingCategory.getImageUrl().isEmpty()) {
                imageUploadService.deleteCategoryImage(existingCategory.getImageUrl());
            }
            String imageUrl = imageUploadService.uploadCategoryImage(image, categoryId);
            categoryService.updateCategoryImage(categoryId, imageUrl);
            updatedCategory = categoryService.getCategoryById(categoryId);
        }

        CategoryResponse response = categoryMapper.toResponse(updatedCategory);
        return success("Category updated successfully", response);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            imageUploadService.deleteCategoryImage(category.getImageUrl());
        }
        categoryService.deleteCategory(categoryId);
        return success("Category deleted successfully", null);
    }

    @PostMapping("/{categoryId}/upload-image")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadCategoryImage(
            @PathVariable Long categoryId,
            @RequestParam("image") MultipartFile image) {

        Category category = categoryService.getCategoryById(categoryId);
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            imageUploadService.deleteCategoryImage(category.getImageUrl());
        }
        String imageUrl = imageUploadService.uploadCategoryImage(image, categoryId);
        categoryService.updateCategoryImage(categoryId, imageUrl);

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        return success("Category image uploaded successfully", response);
    }

    @DeleteMapping("/{categoryId}/delete-image")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryImage(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            imageUploadService.deleteCategoryImage(category.getImageUrl());
            categoryService.updateCategoryImage(categoryId, null);
        }
        return success("Category image deleted successfully", null);
    }
}