package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.mapper.ProductMapper;
import com.ashanhimantha.product_service.repository.ProductRepository;
import com.ashanhimantha.product_service.service.CategoryService;
import com.ashanhimantha.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor // Lombok for clean constructor injection
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService; // We need this to find the category
    private final ProductMapper productMapper;     // Inject our new mapper
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region:ap-southeast-2}")
    private String awsRegion;

    @Override
    public AdminProductResponse createProduct(ProductRequest productRequest, String supplierId) {
        // 1. Find the category by the ID from the request
        Category category = categoryService.getCategoryById(productRequest.getCategoryId());

        // 2. Map the DTO to a Product entity
        Product product = productMapper.toProduct(productRequest);

        // 3. Set the fields that are not in the DTO
        product.setCategory(category);
        product.setSupplierId(supplierId);
        product.setStatus(ProductStatus.PENDING_APPROVAL);
        product.setUnitCost(productRequest.getUnitCost());
        product.setSellingPrice(productRequest.getSellingPrice());// New products must be approved

        // 4. Save the new product
        Product savedProduct = productRepository.save(product);

        // 5. Map the saved entity to a Response DTO and return it
        return productMapper.toAdminProductResponse(savedProduct);
    }

    // PUBLIC method - returns the safe DTO
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllApprovedProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatus(ProductStatus.APPROVED, pageable);
        return productPage.map(productMapper::toProductResponse); // Uses the public mapper method
    }

    @Override
    public ProductResponse getApprovedProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getStatus() == ProductStatus.APPROVED) // Only return if approved
                .orElseThrow(() -> new ResourceNotFoundException("Approved product not found with id: " + productId));

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Category newCategory = categoryService.getCategoryById(productRequest.getCategoryId());

        // Update fields
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setUnitCost(productRequest.getUnitCost());
        existingProduct.setSellingPrice(productRequest.getSellingPrice());
        existingProduct.setStockCount(productRequest.getStockCount());
        existingProduct.setProducerInfo(productRequest.getProducerInfo());
        existingProduct.setCategory(newCategory);
        existingProduct.setStatus(ProductStatus.PENDING_APPROVAL); // Reset status on update

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }

    // --- NEWLY IMPLEMENTED METHODS ---

    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long productId, ProductStatus newStatus) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (newStatus == ProductStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Cannot manually set a product's status to PENDING_APPROVAL.");
        }

        product.setStatus(newStatus);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    public Page<ProductResponse> getProductsByStatus(Pageable pageable, String status) {
        try {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            Page<Product> productPage = productRepository.findByStatus(productStatus, pageable);
            return productPage.map(productMapper::toProductResponse);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid product status provided: " + status);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Page<AdminProductResponse> getProductsBySupplier(String supplierId, Pageable pageable) {
        // Find all products for the given supplierId using the new repository method
        Page<Product> productPage = productRepository.findBySupplierId(supplierId, pageable);

        // Map the results to the detailed AdminProductResponse DTO
        return productPage.map(productMapper::toAdminProductResponse);
    }

    @Override
    @Transactional
    public AdminProductResponse uploadProductImage(Long productId, MultipartFile file, String supplierId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Authorization: allow if supplier owns this product or caller is SuperAdmin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SuperAdmins".equals(a.getAuthority()));

        if (!isSuperAdmin && !product.getSupplierId().equals(supplierId)) {
            throw new AccessDeniedException("You are not allowed to upload image for this product");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String key = String.format("products/%d/%s%s", productId, java.util.UUID.randomUUID(), ext);

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, awsRegion, key);

            product.setImageUrl(url);
            Product saved = productRepository.save(product);

            return productMapper.toAdminProductResponse(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
}
