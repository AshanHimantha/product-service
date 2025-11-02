package com.ashanhimantha.product_service.service;

import com.ashanhimantha.product_service.entity.Product;

/**
 * Service for handling product purchases
 * Demonstrates the usage of the Strategy Pattern for different product types
 */
public interface PurchaseService {

    /**
     * Check if a product can be purchased with the given quantity
     */
    boolean canPurchase(Long productId, Integer quantity);

    /**
     * Calculate the total price for purchasing a product
     */
    Double calculateTotalPrice(Long productId, Integer quantity);

    /**
     * Process a product purchase
     */
    void processPurchase(Long productId, Integer quantity);
}

