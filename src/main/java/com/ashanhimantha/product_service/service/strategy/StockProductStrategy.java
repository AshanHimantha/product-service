package com.ashanhimantha.product_service.service.strategy;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Strategy for products that require stock/inventory tracking
 * All stock products now use variants (color/size) for inventory management
 */
@Component("STOCK")
public class StockProductStrategy implements ProductPricingStrategy {

    @Override
    public void validateProductRequest(ProductRequest request) {
        // Stock products must have variants
        if (!request.hasVariants() || request.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Stock products must have at least one variant");
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
            if (variant.getQuantity() == null) {
                throw new IllegalArgumentException("Initial quantity is required for all variants");
            }
            if (variant.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative for variants");
            }
        }
    }

    @Override
    public void applyPricing(Product product, ProductRequest request) {
        // All stock products use variants for inventory management
        // Variants are created and managed in the service layer
    }

    @Override
    public boolean canPurchase(Product product, Integer quantity) {
        // Check total stock across all variants
        return product.getTotalStock() >= quantity;
    }

    @Override
    public Double calculatePrice(Product product, Integer quantity) {
        // For variant products, you need to specify which variant
        throw new IllegalStateException("Cannot calculate price for variant products without specifying variant");
    }

    @Override
    public void processPurchase(Product product, Integer quantity) {
        // For variant products, stock deduction needs to specify which variant
        throw new IllegalStateException("Cannot process purchase for variant products without specifying variant");
    }
}
