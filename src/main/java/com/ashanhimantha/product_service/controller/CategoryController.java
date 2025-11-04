package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.CategoryRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.CategoryResponse;
import com.ashanhimantha.product_service.dto.response.CategorySummaryResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.mapper.CategoryMapper;
import com.ashanhimantha.product_service.service.CategoryService;
import com.ashanhimantha.product_service.service.ImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
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
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<?>> getAllCategories(
            @RequestParam(value = "summary", required = false, defaultValue = "false") boolean summary) {
        List<Category> categories = categoryService.getAllCategoriesAsList();

        if (summary) {
            List<CategorySummaryResponse> response = categoryMapper.toSummaryResponseList(categories);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) success("Categories summary retrieved successfully", response);
        } else {
            List<CategoryResponse> response = categoryMapper.toResponseList(categories);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) success("Categories retrieved successfully", response);
        }
    }

    @GetMapping("/{categoryId}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<?>> getCategoryById(
            @PathVariable Long categoryId,
            @RequestParam(value = "summary", required = false, defaultValue = "false") boolean summary) {
        Category category = categoryService.getCategoryById(categoryId);

        if (summary) {
            CategorySummaryResponse response = categoryMapper.toSummaryResponse(category);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) success("Category summary retrieved successfully", response);
        } else {
            CategoryResponse response = categoryMapper.toResponse(category);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) success("Category retrieved successfully", response);
        }
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @ModelAttribute CategoryRequest categoryRequest,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Category createdCategory = categoryService.createCategory(categoryRequest);

        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadService.uploadCategoryImage(image, createdCategory.getId());
            categoryService.updateCategoryImage(createdCategory.getId(), imageUrl);
            createdCategory = categoryService.getCategoryById(createdCategory.getId());
        }

        CategoryResponse response = categoryMapper.toResponse(createdCategory);
        return created("Category created successfully", response);
    }

    // JSON endpoint for updating category without image
    @PutMapping(value = "/{categoryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategoryJson(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        Category updatedCategory = categoryService.updateCategory(categoryId, categoryRequest);
        CategoryResponse response = categoryMapper.toResponse(updatedCategory);
        return success("Category updated successfully", response);
    }

    // Multipart endpoint for updating category with optional image
    @PutMapping(value = "/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @ModelAttribute CategoryRequest categoryRequest,
            @RequestParam(value = "image", required = false) MultipartFile image) {

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

        // Check if category has relationships with products
        boolean hasProducts = categoryService.categoryHasProducts(categoryId);

        if (!hasProducts) {
            // Permanent delete: Delete the image from S3 first
            if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
                imageUploadService.deleteCategoryImage(category.getImageUrl());
            }
        }
        // If hasProducts, it's a soft delete - keep the image for historical data

        categoryService.deleteCategory(categoryId);

        String message = hasProducts
            ? "Category deactivated successfully (soft delete)"
            : "Category deleted permanently";

        return success(message, null);
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
