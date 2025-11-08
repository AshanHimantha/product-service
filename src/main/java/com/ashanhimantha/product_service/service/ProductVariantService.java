package com.ashanhimantha.product_service.service;

import com.ashanhimantha.product_service.dto.request.StockUpdateRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.response.ProductVariantResponse;

import java.util.List;

public interface ProductVariantService {

    /**
     * Create a new variant for an existing product
     */
    ProductVariantResponse createVariant(Long productId, VariantRequest request);

    /**
     * Get a specific product variant by ID
     */
    ProductVariantResponse getVariantById(Long variantId);

    /**
     * Get all variants for a specific product
     */
    List<ProductVariantResponse> getVariantsByProductId(Long productId);

    /**
     * Update stock quantity for a specific variant
     */
    ProductVariantResponse updateVariantStock(Long variantId, StockUpdateRequest request);

    /**
     * Update variant active status
     */
    ProductVariantResponse updateVariantStatus(Long variantId, Boolean isActive);

    /**
     * Update variant pricing
     */
    ProductVariantResponse updateVariantPricing(Long variantId, Double unitCost, Double sellingPrice);
}
