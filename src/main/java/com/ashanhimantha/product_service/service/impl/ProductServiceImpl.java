package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.dto.request.ProductPatchRequest;
import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.ProductUpdateRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.dto.response.PublicProductResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.entity.ProductVariant;
import com.ashanhimantha.product_service.entity.enums.Status;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.mapper.ProductMapper;
import com.ashanhimantha.product_service.repository.ProductRepository;
import com.ashanhimantha.product_service.service.CategoryService;
import com.ashanhimantha.product_service.service.ImageUploadService;
import com.ashanhimantha.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final ImageUploadService imageUploadService;

    private static final int MAX_IMAGES = 6;
    private static final String PRODUCT_FOLDER = "products/";

    @Override
    @Transactional
    public AdminProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> files) {
        // Validate product request
        validateProductRequest(productRequest);

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
        product.setStatus(productRequest.getStatus() != null ? productRequest.getStatus() : Status.ACTIVE);
        product.setCategory(category);

        // 4. Save the product FIRST to get an ID (without variants yet)
        Product savedProduct = productRepository.save(product);

        // 5. If product has variants (colors/sizes), create them and link to saved product
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

        // 6. Upload images and return the response
        return uploadProductImages(savedProduct.getId(), validFiles);
    }

    private String generateSKU(Product product, ProductVariant variant) {
        String productName = product.getName().replaceAll("\\s+", "").toUpperCase();
        if (productName.length() > 4) {
            productName = productName.substring(0, 4);
        }
        String color = "NONE";
        if (variant.getColor() != null && !variant.getColor().isBlank()) {
            color = variant.getColor().replaceAll("[^a-zA-Z]", "").toUpperCase();
            if (color.length() > 3) {
                color = color.substring(0, 3);
            }
        }
        String size = variant.getSize().toUpperCase();

        // Use a timestamp component to reduce collisions
        return String.format("%s-%s-%s-%d", productName, color, size, System.currentTimeMillis() % 10000);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatus(Status.ACTIVE, pageable);
        return productPage.map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByStatus(Pageable pageable, String status) {
        try {
            Status productStatus = Status.valueOf(status.toUpperCase());
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
                .filter(p -> p.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active product not found with id: " + productId));

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicProductResponse> getAllActiveProductsForPublic(Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatus(Status.ACTIVE, pageable);
        return productPage.map(productMapper::toPublicProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProductResponse getActiveProductByIdForPublic(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active product not found with id: " + productId));

        return productMapper.toPublicProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest productUpdateRequest, List<MultipartFile> files) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Only update allowed fields: name, description, status
        existingProduct.setName(productUpdateRequest.getName());
        existingProduct.setDescription(productUpdateRequest.getDescription());
        if (productUpdateRequest.getStatus() != null) {
            existingProduct.setStatus(productUpdateRequest.getStatus());
        }

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

    @Transactional
    @Override
    public ProductResponse patchProduct(Long productId, ProductPatchRequest productPatchRequest, List<MultipartFile> files) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Only update fields that are provided (non-null)
        if (productPatchRequest.getName() != null) {
            existingProduct.setName(productPatchRequest.getName());
        }
        if (productPatchRequest.getDescription() != null) {
            existingProduct.setDescription(productPatchRequest.getDescription());
        }
        if (productPatchRequest.getStatus() != null) {
            existingProduct.setStatus(productPatchRequest.getStatus());
        }

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
    public AdminProductResponse updateProductStatusForAdmin(Long productId, Status newStatus) {
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

        // Use centralized ImageUploadService
        List<String> uploadedUrls = imageUploadService.uploadImages(
                validFiles,
                PRODUCT_FOLDER + productId + "/",
                "product"
        );

        product.getImageUrls().addAll(uploadedUrls);
        Product saved = productRepository.save(product);
        return productMapper.toAdminProductResponse(saved);
    }

    private void validateProductRequest(ProductRequest request) {
        // All products must have variants
        if (!request.hasVariants() || request.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Products must have at least one variant");
        }

        // Validate each variant
        for (VariantRequest variant : request.getVariants()) {
            if (variant.getUnitCost() == null) {
                throw new IllegalArgumentException("Unit cost is required for all variants");
            }
            if (variant.getSellingPrice() == null) {
                throw new IllegalArgumentException("Selling price is required for all variants");
            }
            if (variant.getSellingPrice() < variant.getUnitCost()) {
                throw new IllegalArgumentException(
                    String.format("Selling price should not be less than unit cost for variant %s-%s",
                        variant.getColor(), variant.getSize())
                );
            }
            if (variant.getQuantity() != null && variant.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative for variants");
            }
        }
    }
}

