package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.ProductUpdateRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.PaginatedResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.dto.response.PublicProductResponse;
import com.ashanhimantha.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Products management APIs for organizing products")
public class ProductController extends AbstractController {

    private final ProductService productService;
    private static final int MAX_IMAGES = 6;


    @Operation(
            summary = "Get all active products",
            description = "Retrieve all active products for public view with pagination support"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<PublicProductResponse>>> getAllActiveProducts(
            @Parameter(hidden = true) Pageable pageable) {
        Page<PublicProductResponse> productPage = productService.getAllActiveProductsForPublic(pageable);
        PaginatedResponse<PublicProductResponse> responseData = new PaginatedResponse<>(productPage);
        return success("Active products retrieved successfully", responseData);
    }

    @Operation(
            summary = "Get active product by ID",
            description = "Retrieve a specific active product by its ID for public view"
    )
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<PublicProductResponse>> getActiveProductById(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        PublicProductResponse product = productService.getActiveProductByIdForPublic(productId);
        return success("Active product retrieved successfully", product);
    }


    // ========== ADMIN ENDPOINTS ==========

    @Operation(
            summary = "Get all products (Admin)",
            description = "Retrieve all products including inactive ones for admin view with pagination support. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping("/admin")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminProductResponse>>> getAllProductsForAdmin(
            @Parameter(hidden = true) Pageable pageable) {
        Page<AdminProductResponse> productPage = productService.getAllProductsForAdmin(pageable);
        PaginatedResponse<AdminProductResponse> responseData = new PaginatedResponse<>(productPage);
        return success("All products retrieved successfully for admin", responseData);
    }

    @Operation(
            summary = "Get product by ID (Admin)",
            description = "Retrieve a specific product by its ID with full admin details. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping("/admin/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> getProductByIdForAdmin(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        AdminProductResponse product = productService.getProductByIdForAdmin(productId);
        return success("Product retrieved successfully for admin", product);
    }

    @Operation(
            summary = "Create a new product",
            description = "Create a new product with images. Maximum 6 images allowed. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> createProduct(
            @Valid @ModelAttribute ProductRequest productRequest,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        List<MultipartFile> fileList = files == null ? List.of() : Arrays.asList(files);
        if (fileList.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images are allowed per product");
        }

        AdminProductResponse createdProduct = productService.createProduct(productRequest, fileList);
        return created("Product created successfully", createdProduct);
    }

    @Operation(
            summary = "Update a product",
            description = "Update an existing product's name, description, images, and status only. Maximum 6 images allowed. Variants must be managed via the variants controller. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId,
            @Valid @ModelAttribute ProductUpdateRequest productUpdateRequest,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        List<MultipartFile> fileList = files == null ? List.of() : Arrays.asList(files);
        if (fileList.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images are allowed per product");
        }

        ProductResponse updatedProduct = productService.updateProduct(productId, productUpdateRequest, fileList);
        return success("Product updated successfully", updatedProduct);
    }

    @Operation(
            summary = "Delete a product",
            description = "Delete a product by its ID. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        productService.deleteProduct(productId);
        return success("Product deleted successfully", null);
    }

    @Operation(
            summary = "Upload product images",
            description = "Upload additional images for an existing product. Maximum 6 images allowed. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PostMapping("/{productId}/images")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> uploadProductImages(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId,
            @RequestParam("files") MultipartFile[] files) {

        List<MultipartFile> fileList = files == null ? List.of() : Arrays.asList(files);
        if (fileList.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required for upload.");
        }
        if (fileList.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images are allowed per product");
        }

        AdminProductResponse response = productService.uploadProductImages(productId, fileList);
        return success("Product images uploaded successfully", response);
    }
}