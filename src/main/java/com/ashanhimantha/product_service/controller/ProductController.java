package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.UpdateProductStatusRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.PaginatedResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController extends AbstractController {

    private final ProductService productService;

    // --- PUBLIC (CUSTOMER-FACING) ENDPOINTS ---

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> getAllApprovedProducts(Pageable pageable) {
        Page<ProductResponse> productPage = productService.getAllApprovedProducts(pageable);
        PaginatedResponse<ProductResponse> responseData = new PaginatedResponse<>(productPage);
        return success("Approved products retrieved successfully", responseData);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getApprovedProductById(@PathVariable Long productId) {
        ProductResponse product = productService.getApprovedProductById(productId);
        return success("Approved product retrieved successfully", product);
    }

    // --- SUPPLIER-FACING ENDPOINTS ---

    @PostMapping
    @PreAuthorize("hasAnyRole('Suppliers', 'SuperAdmins')") // CORRECTED: Use hasAnyRole
    public ResponseEntity<ApiResponse<AdminProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest productRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String supplierId = jwt.getSubject();
        AdminProductResponse createdProduct = productService.createProduct(productRequest, supplierId);
        return created("Product submitted for approval successfully", createdProduct);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('SuperAdmins') or @productSecurityService.isOwner(#jwt, #productId)") // CORRECTED: Use hasRole
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest productRequest,
            @AuthenticationPrincipal Jwt jwt) {

        ProductResponse updatedProduct = productService.updateProduct(productId, productRequest);
        return success("Product updated and is pending re-approval", updatedProduct);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SuperAdmins') or @productSecurityService.isOwner(#jwt, #productId)") // CORRECTED: Use hasRole
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal Jwt jwt) {

        productService.deleteProduct(productId);
        return noContent("Product deleted successfully");
    }

    // --- DATA STEWARD / ADMIN-FACING ENDPOINTS ---

    @PutMapping("/{productId}/status")
    @PreAuthorize("hasAnyRole('DataStewards', 'SuperAdmins')") // CORRECTED: Use hasAnyRole
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductStatusRequest request) {

        ProductResponse updatedProduct = productService.updateProductStatus(productId, request.getNewStatus());
        return success("Product status updated successfully", updatedProduct);
    }

    @GetMapping("/status/pending")
    @PreAuthorize("hasAnyRole('DataStewards', 'SuperAdmins')") // CORRECTED: Use hasAnyRole
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> getPendingApprovalProducts(Pageable pageable) {
        Page<ProductResponse> productPage = productService.getProductsByStatus(pageable, "PENDING_APPROVAL");
        PaginatedResponse<ProductResponse> responseData = new PaginatedResponse<>(productPage);
        return success("Products pending approval retrieved successfully", responseData);
    }

    @GetMapping("/any/{productId}")
    @PreAuthorize("hasAnyRole('DataStewards', 'SuperAdmins') or @productSecurityService.isOwner(#jwt, #productId)") // CORRECTED: Use hasAnyRole and hasRole
    public ResponseEntity<ApiResponse<ProductResponse>> getAnyProductById(
            @PathVariable Long productId,
            @AuthenticationPrincipal Jwt jwt) {

        ProductResponse product = productService.getAnyProductById(productId);
        return success("Product details retrieved successfully", product);
    }
}