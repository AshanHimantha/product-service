package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.dto.request.StockUpdateRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.response.ProductVariantResponse;
import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.entity.ProductVariant;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.repository.ProductRepository;
import com.ashanhimantha.product_service.repository.ProductVariantRepository;
import com.ashanhimantha.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Override
    public ProductVariantResponse getVariantById(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));
        return mapToResponse(variant);
    }

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(Long productId) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        return variants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariantStock(Long variantId, StockUpdateRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        Integer oldQuantity = variant.getQuantity();
        variant.setQuantity(request.getQuantity());

        ProductVariant updated = productVariantRepository.save(variant);

        log.info("Stock updated for variant ID {}: {} -> {}. Reason: {}",
                variantId, oldQuantity, request.getQuantity(),
                request.getReason() != null ? request.getReason() : "Not specified");

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariantStatus(Long variantId, Boolean isActive) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        variant.setIsActive(isActive);
        ProductVariant updated = productVariantRepository.save(variant);

        log.info("Status updated for variant ID {}: {}", variantId, isActive ? "ACTIVE" : "INACTIVE");

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariantPricing(Long variantId, Double unitCost, Double sellingPrice) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        if (unitCost != null && unitCost >= 0) {
            variant.setUnitCost(unitCost);
        }

        if (sellingPrice != null && sellingPrice > 0) {
            variant.setSellingPrice(sellingPrice);
        }

        ProductVariant updated = productVariantRepository.save(variant);

        log.info("Pricing updated for variant ID {}: Cost={}, Price={}",
                variantId, unitCost, sellingPrice);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ProductVariantResponse createVariant(Long productId, VariantRequest request) {
        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Create new variant
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(request.getColor());
        variant.setSize(request.getSize());
        variant.setUnitCost(request.getUnitCost());
        variant.setSellingPrice(request.getSellingPrice());
        variant.setQuantity(request.getQuantity());
        variant.setSku(request.getSku());
        variant.setIsActive(true);

        try {
            ProductVariant saved = productVariantRepository.save(variant);
            log.info("New variant created for product ID {}: {} - {}", productId,
                    saved.getColor() != null ? saved.getColor() : "No Color", saved.getSize());
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("A variant with the same color and size already exists for this product.");
        }
    }

    /**
     * Map ProductVariant entity to ProductVariantResponse DTO
     */
    private ProductVariantResponse mapToResponse(ProductVariant variant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProduct().getId());
        response.setProductName(variant.getProduct().getName());
        response.setColor(variant.getColor());
        response.setSize(variant.getSize());
        response.setUnitCost(variant.getUnitCost());
        response.setSellingPrice(variant.getSellingPrice());
        response.setQuantity(variant.getQuantity());
        response.setSku(variant.getSku());
        response.setIsActive(variant.getIsActive());
        response.setVariantName(variant.getVariantName());
        response.setCreatedAt(variant.getCreatedAt());
        response.setUpdatedAt(variant.getUpdatedAt());
        return response;
    }
}
