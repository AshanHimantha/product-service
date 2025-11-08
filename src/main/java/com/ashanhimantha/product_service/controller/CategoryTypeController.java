package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.CategoryTypeRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.CategoryTypeResponse;
import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.mapper.CategoryTypeMapper;
import com.ashanhimantha.product_service.service.CategoryTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Category Types", description = "Category type management APIs for defining product classification")
public class CategoryTypeController extends AbstractController {

    private final CategoryTypeService categoryTypeService;
    private final CategoryTypeMapper categoryTypeMapper;

    @Operation(
            summary = "Get all category types",
            description = "Retrieve all category types available in the system"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryTypeResponse>>> getAllCategoryTypes() {
        List<CategoryType> categoryTypes = categoryTypeService.getAllCategoryTypes();
        List<CategoryTypeResponse> response = categoryTypeMapper.toResponseList(categoryTypes);
        return success("Category types retrieved successfully", response);
    }

    @Operation(
            summary = "Get category type by ID",
            description = "Retrieve a single category type by its ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> getCategoryTypeById(
            @Parameter(description = "Category Type ID", required = true) @PathVariable Long id) {
        CategoryType categoryType = categoryTypeService.getCategoryTypeById(id);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(categoryType);
        return success("Category type retrieved successfully", response);
    }

    @Operation(
            summary = "Create a new category type",
            description = "Create a new category type. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PostMapping
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> createCategoryType(
            @Valid @RequestBody CategoryTypeRequest request) {
        CategoryType createdCategoryType = categoryTypeService.createCategoryType(request);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(createdCategoryType);
        return created("Category type created successfully", response);
    }

    @Operation(
            summary = "Update a category type",
            description = "Update an existing category type. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> updateCategoryType(
            @Parameter(description = "Category Type ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CategoryTypeRequest request) {

        System.out.println(request.getStatus());
        CategoryType updatedCategoryType = categoryTypeService.updateCategoryType(id, request);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(updatedCategoryType);
        return success("Category type updated successfully", response);
    }

    @Operation(
            summary = "Update category type status",
            description = "Update the active/inactive status of a category type. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<CategoryTypeResponse>> updateCategoryTypeStatus(
            @Parameter(description = "Category Type ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Status object with 'status' field")
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        CategoryType updatedCategoryType = categoryTypeService.updateCategoryTypeStatus(id, status);
        CategoryTypeResponse response = categoryTypeMapper.toResponse(updatedCategoryType);
        return success("Category type status updated successfully", response);
    }

    @Operation(
            summary = "Delete a category type",
            description = "Delete a category type (soft delete if has categories, hard delete otherwise). Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryType(
            @Parameter(description = "Category Type ID", required = true) @PathVariable Long id) {
        categoryTypeService.deleteCategoryType(id);
        return success("Category type deleted successfully", null);
    }
}
