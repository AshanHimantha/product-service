package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.dto.response.PublicProductResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.entity.ProductVariant;
import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.mapper.ProductMapper;
import com.ashanhimantha.product_service.repository.ProductRepository;
import com.ashanhimantha.product_service.service.CategoryService;
import com.ashanhimantha.product_service.service.ProductService;
import com.ashanhimantha.product_service.service.strategy.ProductPricingStrategy;
import com.ashanhimantha.product_service.service.strategy.ProductPricingStrategyFactory;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final S3Client s3Client;
    private final ProductPricingStrategyFactory strategyFactory;

    @Value("${aws.s3.bucket-name}")
    private String bucket;

    @Value("${aws.region:ap-southeast-2}")
    private String awsRegion;

    private static final int MAX_IMAGES = 6;

    @Override
    @Transactional
    public AdminProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> files) {
        // Validate using the appropriate strategy
        ProductPricingStrategy strategy = strategyFactory.getStrategy(productRequest.getProductType());
        strategy.validateProductRequest(productRequest);

        // Filter out empty/null files before counting
        List<MultipartFile> validFiles = files != null ?
                files.stream().filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList())
                : List.of();

        if (validFiles.isEmpty()) {
            throw new IllegalArgumentException("At least 1 valid image is required when creating a product");
        }
        if (validFiles.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Cannot add " + validFiles.size() + " images. Maximum allowed is " + MAX_IMAGES + " images.");
        }

        // 1. Find the category by the ID from the request
        Category category = categoryService.getCategoryById(productRequest.getCategoryId());

        // 2. Map the DTO to a Product entity
        Product product = productMapper.toProduct(productRequest);

        // 3. Set the fields that are not in the DTO
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(category);

        // 4. Apply pricing strategy based on product type (creates Stock entity if needed)
        strategy.applyPricing(product, productRequest);

        // 5. Save the product FIRST to get an ID (without variants yet)
        Product savedProduct = productRepository.save(product);

        // 6. If product has variants (colors/sizes), create them and link to saved product
        if (productRequest.hasVariants()) {
            for (VariantRequest variantRequest : productRequest.getVariants()) {
                ProductVariant variant = productMapper.toProductVariant(variantRequest);
                variant.setProduct(savedProduct); // Link variant to the product

                // Generate SKU if not provided
                if (variant.getSku() == null || variant.getSku().isBlank()) {
                    variant.setSku(generateSKU(savedProduct, variant));
                }
                savedProduct.getVariants().add(variant);
            }
            // Save again to persist variants
            savedProduct = productRepository.save(savedProduct);
        }

        // 7. Upload images and return the response
        return uploadProductImages(savedProduct.getId(), validFiles);
    }

    /**
     * Generates a simple Stock Keeping Unit (SKU) for a product variant.
     * Example: PROD-RED-SML-1234
     * @param product The parent product.
     * @param variant The specific variant.
     * @return A generated SKU string.
     */
    private String generateSKU(Product product, ProductVariant variant) {
        String productName = product.getName().replaceAll("\\s+", "").toUpperCase();
        if (productName.length() > 4) {
            productName = productName.substring(0, 4);
        }
        String color = variant.getColor().replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (color.length() > 3) {
            color = color.substring(0, 3);
        }
        String size = variant.getSize().toUpperCase();

        // Use a timestamp component to reduce collisions
        return String.format("%s-%s-%s-%d", productName, color, size, System.currentTimeMillis() % 10000);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return productPage.map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByStatus(Pageable pageable, String status) {
        try {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            Page<Product> productPage = productRepository.findByStatus(productStatus, pageable);
            return productPage.map(productMapper::toProductResponse);
        } catch (IllegalArgumentException e) {
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
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> getAllActiveProductsForPublic(Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return productPage.map(productMapper::toPublicProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProductResponse getActiveProductByIdForPublic(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active product not found with id: " + productId));

        return productMapper.toPublicProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest productRequest, List<MultipartFile> files) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Validate using the appropriate strategy
        ProductPricingStrategy strategy = strategyFactory.getStrategy(productRequest.getProductType());
        strategy.validateProductRequest(productRequest);

        Category newCategory = categoryService.getCategoryById(productRequest.getCategoryId());

        // Update fields
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setProductType(productRequest.getProductType());
        existingProduct.setCategory(newCategory);

        // Apply pricing strategy based on product type
        strategy.applyPricing(existingProduct, productRequest);

        // Note: This update method does not handle updating/adding/removing variants.
        // A more complex implementation would be needed for that.

        Product updatedProduct = productRepository.save(existingProduct);

        // If files provided, upload them
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> validFiles = files.stream().filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());
            if (!validFiles.isEmpty()) {
                int currentImageCount = existingProduct.getImageUrls().size();
                int newImageCount = validFiles.size();
                if (currentImageCount + newImageCount > MAX_IMAGES) {
                    throw new IllegalArgumentException(
                            "Cannot add " + newImageCount + " images. Product already has " +
                                    currentImageCount + " images. Maximum allowed is " + MAX_IMAGES + " images.");
                }

                uploadProductImages(productId, validFiles);
                Product reloaded = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
                return productMapper.toProductResponse(reloaded);
            }
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
            throw new IllegalArgumentException("At least one file is required for upload");
        }

        List<MultipartFile> validFiles = files.stream().filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());
        if (validFiles.isEmpty()) {
            throw new IllegalArgumentException("No valid files provided for upload");
        }

        int currentImageCount = product.getImageUrls().size();
        int newImageCount = validFiles.size();

        if (currentImageCount + newImageCount > MAX_IMAGES) {
            throw new IllegalArgumentException(
                    "Cannot add " + newImageCount + " images. Product already has " +
                            currentImageCount + " images. Maximum allowed is " + MAX_IMAGES + " images.");
        }

        try {
            return processUploadProductImages(product, validFiles);
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