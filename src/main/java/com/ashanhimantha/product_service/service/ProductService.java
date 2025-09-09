package com.ashanhimantha.product_service.service;



import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.entity.enums.ProductStatus; // Import enum
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {


    AdminProductResponse createProduct(ProductRequest productRequest, String supplierId);
    ProductResponse updateProduct(Long productId, ProductRequest productRequest);
    void deleteProduct(Long productId);
    Page<ProductResponse> getAllApprovedProducts(Pageable pageable);
    ProductResponse getApprovedProductById(Long productId);
    ProductResponse updateProductStatus(Long productId, ProductStatus newStatus);
    Page<ProductResponse> getProductsByStatus(Pageable pageable, String status);
    ProductResponse getAnyProductById(Long productId);
}