package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.repository.ProductRepository;
import com.ashanhimantha.product_service.service.PurchaseService;
import com.ashanhimantha.product_service.service.strategy.ProductPricingStrategy;
import com.ashanhimantha.product_service.service.strategy.ProductPricingStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of PurchaseService demonstrating the Strategy Pattern usage
 */
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final ProductRepository productRepository;
    private final ProductPricingStrategyFactory strategyFactory;

    @Override
    @Transactional(readOnly = true)
    public boolean canPurchase(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Get the appropriate strategy based on product type
        ProductPricingStrategy strategy = strategyFactory.getStrategy(product.getProductType());

        // Delegate to the strategy
        return strategy.canPurchase(product, quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalPrice(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Get the appropriate strategy based on product type
        ProductPricingStrategy strategy = strategyFactory.getStrategy(product.getProductType());

        // Delegate to the strategy
        return strategy.calculatePrice(product, quantity);
    }

    @Override
    @Transactional
    public void processPurchase(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Get the appropriate strategy based on product type
        ProductPricingStrategy strategy = strategyFactory.getStrategy(product.getProductType());

        // Check if purchase is possible
        if (!strategy.canPurchase(product, quantity)) {
            throw new IllegalStateException("Cannot purchase product. Check stock availability.");
        }

        // Process the purchase (updates stock for STOCK products, does nothing for NON_STOCK)
        strategy.processPurchase(product, quantity);

        // Save the updated product (with updated stock if applicable)
        productRepository.save(product);
    }
}

