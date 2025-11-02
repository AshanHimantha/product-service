package com.ashanhimantha.product_service.service.strategy;

import com.ashanhimantha.product_service.entity.enums.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for obtaining the appropriate pricing strategy based on product type
 */
@Component
@RequiredArgsConstructor
public class ProductPricingStrategyFactory {

    private final Map<String, ProductPricingStrategy> strategies;

    /**
     * Get the appropriate strategy for the given product type
     */
    public ProductPricingStrategy getStrategy(ProductType productType) {
        ProductPricingStrategy strategy = strategies.get(productType.name());
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for product type: " + productType);
        }
        return strategy;
    }
}

