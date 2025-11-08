package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.StockUpdateRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.ProductVariantResponse;
import com.ashanhimantha.product_service.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-variants")
@RequiredArgsConstructor
public class ProductVariantController extends AbstractController {

    private final ProductVariantService productVariantService;

    /**
     * Create a new variant for an existing product
     * Example: POST /api/v1/product-variants/product/123
     * Body: { "color": "Red", "size": "M", "unitCost": 15.00, "sellingPrice": 29.99, "quantity": 50 }
     */
    @PostMapping("/product/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody VariantRequest request) {
        ProductVariantResponse variant = productVariantService.createVariant(productId, request);
        return created("Product variant created successfully", variant);
    }

    /**
     * Get a specific product variant by ID
     */
    @GetMapping("/{variantId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(@PathVariable Long variantId) {
        ProductVariantResponse variant = productVariantService.getVariantById(variantId);
        return success("Product variant retrieved successfully", variant);
    }

    /**
     * Get all variants for a specific product
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProductId(@PathVariable Long productId) {
        List<ProductVariantResponse> variants = productVariantService.getVariantsByProductId(productId);
        return success("Product variants retrieved successfully", variants);
    }

    /**
     * Update stock quantity for a specific variant
     * Example: PATCH /api/v1/product-variants/123/stock
     * Body: { "quantity": 50, "reason": "RESTOCKED" }
     */
    @PatchMapping("/{variantId}/stock")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantStock(
            @PathVariable Long variantId,
            @Valid @RequestBody StockUpdateRequest request) {
        ProductVariantResponse updated = productVariantService.updateVariantStock(variantId, request);
        return success("Stock quantity updated successfully", updated);
    }

    /**
     * Update variant active status
     * Example: PATCH /api/v1/product-variants/123/status?isActive=false
     */
    @PatchMapping("/{variantId}/status")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantStatus(
            @PathVariable Long variantId,
            @RequestParam Boolean isActive) {
        ProductVariantResponse updated = productVariantService.updateVariantStatus(variantId, isActive);
        return success("Variant status updated successfully", updated);
    }

    /**
     * Update variant pricing (unit cost and/or selling price)
     * Example: PATCH /api/v1/product-variants/123/pricing?unitCost=15.50&sellingPrice=29.99
     */
    @PatchMapping("/{variantId}/pricing")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantPricing(
            @PathVariable Long variantId,
            @RequestParam(required = false) Double unitCost,
            @RequestParam(required = false) Double sellingPrice) {

        if (unitCost == null && sellingPrice == null) {
            throw new IllegalArgumentException("At least one pricing field (unitCost or sellingPrice) must be provided");
        }

        ProductVariantResponse updated = productVariantService.updateVariantPricing(variantId, unitCost, sellingPrice);
        return success("Variant pricing updated successfully", updated);
    }
}
