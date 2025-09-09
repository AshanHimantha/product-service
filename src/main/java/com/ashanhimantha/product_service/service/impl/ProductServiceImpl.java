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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Lombok for clean constructor injection
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService; // We need this to find the category
    private final ProductMapper productMapper;     // Inject our new mapper

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
    public ProductResponse getAnyProductById(Long productId) {
        // This method finds a product by ID without checking its status.
        // The security check (@PreAuthorize) on the controller ensures only authorized users can call this.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return productMapper.toProductResponse(product);
    }
}
