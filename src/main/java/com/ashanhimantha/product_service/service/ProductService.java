package com.ashanhimantha.product_service.service;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.ProductUpdateRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.dto.response.PublicProductResponse;
import com.ashanhimantha.product_service.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    AdminProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> files);
    ProductResponse updateProduct(Long productId, ProductUpdateRequest productUpdateRequest, List<MultipartFile> files);
    void deleteProduct(Long productId);
    Page<ProductResponse> getAllActiveProducts(Pageable pageable);
    ProductResponse getActiveProductById(Long productId);

    // Public-facing endpoints (for customers)
    Page<PublicProductResponse> getAllActiveProductsForPublic(Pageable pageable);
    PublicProductResponse getActiveProductByIdForPublic(Long productId);

    // Upload one or more product images to S3 and associate the URLs with the product
    AdminProductResponse uploadProductImages(Long productId, List<MultipartFile> files);

    // Admin endpoints for full product details
    Page<AdminProductResponse> getAllProductsForAdmin(Pageable pageable);
    AdminProductResponse getProductByIdForAdmin(Long productId);
    AdminProductResponse updateProductStatusForAdmin(Long productId, Status newStatus);
    Page<ProductResponse> getProductsByStatus(Pageable pageable, String status);
}