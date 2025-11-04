package com.ashanhimantha.product_service.service.strategy;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Strategy for non-stock products (services, digital goods, etc.)
 * These products don't have inventory tracking and are always available
 * Prices are stored in variants
 */
@Component("NON_STOCK")
public class NonStockProductStrategy implements ProductPricingStrategy {

    @Override
    public void validateProductRequest(ProductRequest request) {
        // Non-stock products must have variants (e.g., pizza sizes: Small, Medium, Large)
        // Each variant represents a different option/tier without physical inventory
        if (!request.hasVariants() || request.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Non-stock products must have at least one variant");
        }

        // Validate each variant
        for (var variant : request.getVariants()) {
            if (variant.getUnitCost() == null) {
                throw new IllegalArgumentException("Unit cost is required for all variants");
            }
            if (variant.getSellingPrice() == null) {
                throw new IllegalArgumentException("Selling price is required for all variants");
            }
            if (variant.getSellingPrice() < variant.getUnitCost()) {
                throw new IllegalArgumentException(
                    String.format("Selling price should not be less than unit cost for variant %s",
                        variant.getSize() != null ? variant.getSize() : variant.getColor())
                );
            }
            // Quantity validation: for non-stock, quantity can be null or 0 (infinite availability)
            if (variant.getQuantity() != null && variant.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative for variants");
            }
        }
    }

    @Override
    public void applyPricing(Product product, ProductRequest request) {
        // All non-stock products use variants for pricing
        // Variants are created and managed in the service layer
    }

    @Override
    public boolean canPurchase(Product product, Integer quantity) {
        // Non-stock products are always available (e.g., services, digital downloads, made-to-order items)
        // No inventory check needed
        return true;
    }

    @Override
    public Double calculatePrice(Product product, Integer quantity) {
        // For variant products, you need to specify which variant
        throw new IllegalStateException("Cannot calculate price for variant products without specifying variant");
    }

    @Override
    public void processPurchase(Product product, Integer quantity) {
        // Non-stock products don't require inventory management
        // No inventory deduction needed (always available)
        // Could log purchases, track orders, or trigger fulfillment processes here
    }
}
