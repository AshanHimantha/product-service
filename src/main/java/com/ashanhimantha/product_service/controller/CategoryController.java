package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.CategoryRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.PaginatedResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor // Use Lombok for clean constructor injection
public class CategoryController extends AbstractController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Category>>> getAllCategories(Pageable pageable) {

        Page<Category> categoryPage = categoryService.getAllCategories(pageable);
        PaginatedResponse<Category> responseData = new PaginatedResponse<>(categoryPage);
        return success("Categories retrieved successfully", responseData);

    }

    // (Bonus) Endpoint for getting a simple list for UI dropdowns
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategoriesAsList() {
        List<Category> categories = categoryService.getAllCategoriesAsList();
        return success("Category list retrieved successfully", categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long categoryId) {
        // The service layer handles the "not found" logic now
        Category category = categoryService.getCategoryById(categoryId);
        return success("Category retrieved successfully", category);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        // The service layer handles the "duplicate name" logic now
        Category createdCategory = categoryService.createCategory(categoryRequest);
        return created("Category created successfully", createdCategory);
    }
}