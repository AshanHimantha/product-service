package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.PaginatedResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.dto.response.PublicProductResponse;
import com.ashanhimantha.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController extends AbstractController {

    private final ProductService productService;
    private static final int MAX_IMAGES = 6;


    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<PublicProductResponse>>> getAllActiveProducts(Pageable pageable) {
        Page<PublicProductResponse> productPage = productService.getAllActiveProductsForPublic(pageable);
        PaginatedResponse<PublicProductResponse> responseData = new PaginatedResponse<>(productPage);
        return success("Active products retrieved successfully", responseData);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<PublicProductResponse>> getActiveProductById(@PathVariable Long productId) {
        PublicProductResponse product = productService.getActiveProductByIdForPublic(productId);
        return success("Active product retrieved successfully", product);
    }


    // ========== ADMIN ENDPOINTS ==========

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminProductResponse>>> getAllProductsForAdmin(Pageable pageable) {
        Page<AdminProductResponse> productPage = productService.getAllProductsForAdmin(pageable);
        PaginatedResponse<AdminProductResponse> responseData = new PaginatedResponse<>(productPage);
        return success("All products retrieved successfully for admin", responseData);
    }

    @GetMapping("/admin/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> getProductByIdForAdmin(@PathVariable Long productId) {
        AdminProductResponse product = productService.getProductByIdForAdmin(productId);
        return success("Product retrieved successfully for admin", product);
    }


    // ========== END ADMIN ENDPOINTS ==========
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> createProduct(
            @Valid @ModelAttribute ProductRequest productRequest,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        java.util.List<MultipartFile> fileList = files == null ? java.util.List.of() : java.util.Arrays.asList(files);
        if (fileList.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images are allowed per product");
        }

        AdminProductResponse createdProduct = productService.createProduct(productRequest, fileList);
        return created("Product created successfully", createdProduct);
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @ModelAttribute ProductRequest productRequest,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        java.util.List<MultipartFile> fileList = files == null ? java.util.List.of() : java.util.Arrays.asList(files);
        if (fileList.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images are allowed per product");
        }

        ProductResponse updatedProduct = productService.updateProduct(productId, productRequest, fileList);
        return success("Product updated successfully", updatedProduct);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return success("Product deleted successfully", null);
    }

    @PostMapping("/{productId}/images")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("files") MultipartFile[] files) {

        java.util.List<MultipartFile> fileList = files == null ? java.util.List.of() : java.util.Arrays.asList(files);
        if (fileList.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images are allowed per product");
        }

        AdminProductResponse response = productService.uploadProductImages(productId, fileList);
        return success("Product images uploaded successfully", response);
    }



}
