package com.ashanhimantha.product_service.service.strategy;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Strategy for non-stock products (services, digital goods, etc.)
 * These products don't have inventory tracking and are always available
 * Prices are stored in Stock entity even for non-stock products for consistency
 */
@Component("NON_STOCK")
public class NonStockProductStrategy implements ProductPricingStrategy {

    @Override
    public void validateProductRequest(ProductRequest request) {
        // Non-stock products can have variants (e.g., pizza sizes: Small, Medium, Large)
        // Each variant represents a different option/tier without physical inventory
        if (request.hasVariants()) {
            if (request.getVariants().isEmpty()) {
                throw new IllegalArgumentException("Variants list cannot be empty for variant products");
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
        // For simple non-stock products without variants, pricing validation happens in service layer
    }

    @Override
    public void applyPricing(Product product, ProductRequest request) {
        // Non-stock products can have variants (e.g., Small/Medium/Large pizza)
        // or use Stock entity for simple products (e.g., a service with one price)
        if (!request.hasVariants()) {
            // Simple non-stock product - Stock entity for pricing will be created in service layer
            // We need to ensure the product doesn't have a stock entity yet
            product.setStock(null); // Will be created and populated in service layer
        } else {
            // Variant-based non-stock product (e.g., pizza sizes)
            // Clear single stock - variants will handle pricing
            // ProductVariant entities will be created in service layer
            product.setStock(null);
        }
    }

    @Override
    public boolean canPurchase(Product product, Integer quantity) {
        // Non-stock products are always available (e.g., services, digital downloads, made-to-order items)
        // No inventory check needed
        return true;
    }

    @Override
    public Double calculatePrice(Product product, Integer quantity) {
        // For non-stock products, get price from Stock entity or variants
        if (product.hasVariants()) {
            // For variant products (e.g., pizza sizes), need to specify which variant
            // This is a simplified calculation - real implementation would need variant selection
            throw new IllegalStateException("Cannot calculate price for variant products without specifying variant");
        } else {
            // For simple non-stock products, get price from Stock entity
            if (product.getStock() != null) {
                return product.getStock().getSellingPrice() * quantity;
            }
            throw new IllegalStateException("Non-stock product must have stock entity for pricing");
        }
    }

    @Override
    public void processPurchase(Product product, Integer quantity) {
        // Non-stock products don't require inventory management
        // No inventory deduction needed (always available)
        // Could log purchases, track orders, or trigger fulfillment processes here
    }
}
