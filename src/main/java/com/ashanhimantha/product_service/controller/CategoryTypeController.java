package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.CategoryTypeRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.CategoryTypeResponse;
import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.mapper.CategoryTypeMapper;
import com.ashanhimantha.product_service.service.CategoryTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/category-types")
@RequiredArgsConstructor
public class CategoryTypeController extends AbstractController {

    private final CategoryTypeService categoryTypeService;
    private final CategoryTypeMapper categoryTypeMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryTypeResponse>>> getAllCategoryTypes() {
        List<CategoryType> categoryTypes = categoryTypeService.getAllCategoryTypes();
        List<CategoryTypeResponse> response = categoryTypeMapper.toResponseList(categoryTypes);
        return success("Category types retrieved successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> getCategoryTypeById(@PathVariable Long id) {
        CategoryType categoryType = categoryTypeService.getCategoryTypeById(id);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(categoryType);
        return success("Category type retrieved successfully", response);
    }

    @PostMapping
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> createCategoryType(
            @Valid @RequestBody CategoryTypeRequest request) {
        CategoryType createdCategoryType = categoryTypeService.createCategoryType(request);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(createdCategoryType);
        return created("Category type created successfully", response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> updateCategoryType(
            @PathVariable Long id,
            @Valid @RequestBody CategoryTypeRequest request) {

        System.out.println(request.getStatus());
        CategoryType updatedCategoryType = categoryTypeService.updateCategoryType(id, request);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(updatedCategoryType);
        return success("Category type updated successfully", response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> updateCategoryTypeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        CategoryType updatedCategoryType = categoryTypeService.updateCategoryTypeStatus(id, status);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(updatedCategoryType);
        return success("Category type status updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryType(@PathVariable Long id) {
        categoryTypeService.deleteCategoryType(id);
        return success("Category type deleted successfully", null);
    }
}
