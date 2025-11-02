package com.ashanhimantha.product_service.service.strategy;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.entity.Product;

/**
 * Strategy interface for handling different product pricing logic
 * based on product type (STOCK vs NON_STOCK)
 */
public interface ProductPricingStrategy {

    /**
     * Validate the product request based on product type rules
     */
    void validateProductRequest(ProductRequest request);

    /**
     * Apply pricing logic to the product
     */
    void applyPricing(Product product, ProductRequest request);

    /**
     * Validate if product can be purchased (e.g., check stock availability)
     */
    boolean canPurchase(Product product, Integer quantity);

    /**
     * Calculate final price for a quantity
     */
    Double calculatePrice(Product product, Integer quantity);

    /**
     * Update stock/inventory after purchase
     */
    void processPurchase(Product product, Integer quantity);
}

