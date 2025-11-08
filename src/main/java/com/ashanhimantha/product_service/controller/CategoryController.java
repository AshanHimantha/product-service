package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.CategoryRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.CategoryResponse;
import com.ashanhimantha.product_service.dto.response.CategorySummaryResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.mapper.CategoryMapper;
import com.ashanhimantha.product_service.service.CategoryService;
import com.ashanhimantha.product_service.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
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
@Tag(name = "Categories", description = "Category management APIs for organizing products")
public class CategoryController extends AbstractController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final ImageUploadService imageUploadService;

    @Operation(
            summary = "Get all categories",
            description = "Retrieve all categories with optional summary format. Use summary=true for basic info only."
    )
    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<?>> getAllCategories(
            @Parameter(description = "Return summary format (id, name, image only)", example = "false")
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

    @Operation(
            summary = "Get category by ID",
            description = "Retrieve a single category by its ID with optional summary format"
    )
    @GetMapping("/{categoryId}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<?>> getCategoryById(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "Return summary format", example = "false")
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


    @Operation(
            summary = "Create a new category",
            description = "Create a new category with optional image. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @ModelAttribute CategoryRequest categoryRequest,
            @Parameter(description = "Category image file")
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

    @Operation(
            summary = "Update category (JSON)",
            description = "Update category details without image. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PutMapping(value = "/{categoryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategoryJson(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        Category updatedCategory = categoryService.updateCategory(categoryId, categoryRequest);
        CategoryResponse response = categoryMapper.toResponse(updatedCategory);
        return success("Category updated successfully", response);
    }

    @Operation(
            summary = "Update category with image",
            description = "Update category details with optional new image. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PutMapping(value = "/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId,
            @Valid @ModelAttribute CategoryRequest categoryRequest,
            @Parameter(description = "New category image file")
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

    @Operation(
            summary = "Delete a category",
            description = "Delete a category (soft delete if has products, hard delete otherwise). Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId) {
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

    @Operation(
            summary = "Upload category image",
            description = "Upload or replace the image for an existing category. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PostMapping("/{categoryId}/upload-image")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadCategoryImage(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "Category image file", required = true)
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

    @Operation(
            summary = "Delete category image",
            description = "Remove the image from a category. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @DeleteMapping("/{categoryId}/delete-image")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryImage(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            imageUploadService.deleteCategoryImage(category.getImageUrl());
            categoryService.updateCategoryImage(categoryId, null);
        }
        return success("Category image deleted successfully", null);
    }

    @Operation(
            summary = "Update category status",
            description = "Update the active/inactive status of a category. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PatchMapping("/{categoryId}/status")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategoryStatus(
            @Parameter(description = "Category ID", required = true) @PathVariable Long categoryId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Status object with 'status' field")
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        Category updatedCategory = categoryService.updateCategoryStatus(categoryId, status);
        CategoryResponse response = categoryMapper.toResponse(updatedCategory);
        return success("Category status updated successfully", response);
    }
}
