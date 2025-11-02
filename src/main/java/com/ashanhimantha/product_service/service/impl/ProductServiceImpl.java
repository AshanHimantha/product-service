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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region:ap-southeast-2}")
    private String awsRegion;

    private static final int MAX_IMAGES = 6;

    @Override
    @Transactional
    public AdminProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> files) {
        // Validate that at least 1 image is provided
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least 1 image is required when creating a product");
        }

        // Validate maximum images limit
        int imageCount = (int) files.stream().filter(f -> f != null && !f.isEmpty()).count();
        if (imageCount == 0) {
            throw new IllegalArgumentException("At least 1 valid image is required when creating a product");
        }
        if (imageCount > MAX_IMAGES) {
            throw new IllegalArgumentException("Cannot add " + imageCount + " images. Maximum allowed is " + MAX_IMAGES + " images.");
        }

        // 1. Find the category by the ID from the request
        Category category = categoryService.getCategoryById(productRequest.getCategoryId());

        // 2. Map the DTO to a Product entity
        Product product = productMapper.toProduct(productRequest);

        // 3. Set the fields that are not in the DTO
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(category);

        // 4. Save the new product
        Product savedProduct = productRepository.save(product);

        // 5. Upload images and return the response
        return uploadProductImages(savedProduct.getId(), files);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return productPage.map(productMapper::toProductResponse);
    }

    /**
     * NEWLY ADDED METHOD IMPLEMENTATION
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByStatus(Pageable pageable, String status) {
        try {
            // Convert the status string to the ProductStatus enum
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            Page<Product> productPage = productRepository.findByStatus(productStatus, pageable);
            return productPage.map(productMapper::toProductResponse);
        } catch (IllegalArgumentException e) {
            // Handle cases where the status string is not a valid enum constant
            throw new IllegalArgumentException("Invalid product status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getActiveProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active product not found with id: " + productId));

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest productRequest, List<MultipartFile> files) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Category newCategory = categoryService.getCategoryById(productRequest.getCategoryId());

        // Update fields
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setUnitCost(productRequest.getUnitCost());
        existingProduct.setSellingPrice(productRequest.getSellingPrice());
        existingProduct.setCategory(newCategory);

        Product updatedProduct = productRepository.save(existingProduct);

        // If files provided, upload them which will persist changes and attach image URLs
        if (files != null && !files.isEmpty()) {
            // Check if adding new images exceeds the limit
            int currentImageCount = existingProduct.getImageUrls().size();
            int newImageCount = files.size();
            if (currentImageCount + newImageCount > MAX_IMAGES) {
                throw new IllegalArgumentException(
                        "Cannot add " + newImageCount + " images. Product already has " +
                                currentImageCount + " images. Maximum allowed is " + MAX_IMAGES + " images.");
            }

            uploadProductImages(productId, files);
            // Reload the product to include newly added image URLs
            Product reloaded = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            return productMapper.toProductResponse(reloaded);
        }

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

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProductResponse> getAllProductsForAdmin(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(productMapper::toAdminProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductResponse getProductByIdForAdmin(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return productMapper.toAdminProductResponse(product);
    }

    @Override
    @Transactional
    public AdminProductResponse updateProductStatusForAdmin(Long productId, ProductStatus newStatus) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStatus(newStatus);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toAdminProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public AdminProductResponse uploadProductImages(Long productId, List<MultipartFile> files) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required");
        }

        // Validate maximum images limit
        int currentImageCount = product.getImageUrls().size();
        int newImageCount = (int) files.stream().filter(f -> f != null && !f.isEmpty()).count();

        if (currentImageCount + newImageCount > MAX_IMAGES) {
            throw new IllegalArgumentException(
                    "Cannot add " + newImageCount + " images. Product already has " +
                            currentImageCount + " images. Maximum allowed is " + MAX_IMAGES + " images.");
        }

        try {
            return processUploadProductImages(product, files);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file(s) to S3", e);
        }
    }

    private AdminProductResponse processUploadProductImages(Product product, List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String key = String.format("products/%d/%s%s", product.getId(), UUID.randomUUID(), ext);

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, awsRegion, key);
            product.getImageUrls().add(url);
        }

        Product saved = productRepository.save(product);
        return productMapper.toAdminProductResponse(saved);
    }
}