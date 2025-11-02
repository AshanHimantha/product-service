package com.ashanhimantha.product_service.service.strategy;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.entity.Stock;
import org.springframework.stereotype.Component;

/**
 * Strategy for products that require stock/inventory tracking
 * Supports both simple products and products with variants (color/size)
 */
@Component("STOCK")
public class StockProductStrategy implements ProductPricingStrategy {

    @Override
    public void validateProductRequest(ProductRequest request) {
        // If product has variants, validate them
        if (request.hasVariants()) {
            if (request.getVariants().isEmpty()) {
                throw new IllegalArgumentException("Variants list cannot be empty for variant products");
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
        // For simple STOCK products without variants, pricing validation happens in service layer
        // where Stock entity with pricing and quantity is created
    }

    @Override
    public void applyPricing(Product product, ProductRequest request) {
        // If product has variants, we don't create a single Stock entity
        // Instead, stock and pricing are managed per variant
        if (!request.hasVariants()) {
            // Initialize stock for simple products (pricing will be set in service layer)
            if (product.getStock() == null) {
                Stock stock = new Stock();
                stock.setProduct(product);
                stock.setQuantity(0);
                stock.setReorderLevel(10);
                stock.setReorderQuantity(50);
                // unitCost and sellingPrice will be set in service layer
                product.setStock(stock);
            }
        } else {
            // Clear single stock for variant products
            product.setStock(null);
        }
    }

    @Override
    public boolean canPurchase(Product product, Integer quantity) {
        if (product.hasVariants()) {
            // For variant products, check total stock across all variants
            return product.getTotalStock() >= quantity;
        } else {
            // For simple products, check the stock entity
            if (product.getStock() == null) {
                return false;
            }
            return product.getStock().getQuantity() >= quantity;
        }
    }

    @Override
    public Double calculatePrice(Product product, Integer quantity) {
        // For stock products, price is based on selling price
        if (product.hasVariants()) {
            // For variant products, you'd typically need to specify which variant
            // This is a simplified calculation - real implementation would need variant selection
            throw new IllegalStateException("Cannot calculate price for variant products without specifying variant");
        } else {
            // For simple products, get price from Stock entity
            if (product.getStock() != null) {
                return product.getStock().getSellingPrice() * quantity;
            }
            throw new IllegalStateException("Stock product must have stock entity for pricing");
        }
    }

    @Override
    public void processPurchase(Product product, Integer quantity) {
        if (product.hasVariants()) {
            // For variant products, stock deduction needs to specify which variant
            throw new IllegalStateException("Cannot process purchase for variant products without specifying variant");
        } else {
            // For simple products, deduct from stock
            if (product.getStock() == null) {
                throw new IllegalStateException("Stock entity not found");
            }

            Stock stock = product.getStock();
            int newQuantity = stock.getQuantity() - quantity;

            if (newQuantity < 0) {
                throw new IllegalStateException("Insufficient stock");
            }

            stock.setQuantity(newQuantity);
        }
    }
}
