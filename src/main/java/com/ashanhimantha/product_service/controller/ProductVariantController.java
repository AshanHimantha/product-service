package com.ashanhimantha.product_service.controller;

import com.ashanhimantha.product_service.dto.request.StockUpdateRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.response.ApiResponse;
import com.ashanhimantha.product_service.dto.response.ProductVariantResponse;
import com.ashanhimantha.product_service.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-variants")
@RequiredArgsConstructor
@Tag(name = "Product Variants", description = "Product variant management APIs for handling SKUs, colors, sizes, and inventory")
public class ProductVariantController extends AbstractController {

    private final ProductVariantService productVariantService;

    /**
     * Create a new variant for an existing product
     * Example: POST /api/v1/product-variants/product/123
     * Body: { "color": "Red", "size": "M", "unitCost": 15.00, "sellingPrice": 29.99, "quantity": 50 }
     */
    @Operation(
            summary = "Create a new product variant",
            description = "Create a new variant (SKU) for an existing product with specific attributes like color, size, and pricing. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PostMapping("/product/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId,
            @Valid @RequestBody VariantRequest request) {
        ProductVariantResponse variant = productVariantService.createVariant(productId, request);
        return created("Product variant created successfully", variant);
    }

    /**
     * Get a specific product variant by ID
     */
    @Operation(
            summary = "Get variant by ID",
            description = "Retrieve a specific product variant by its ID. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping("/{variantId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId) {
        ProductVariantResponse variant = productVariantService.getVariantById(variantId);
        return success("Product variant retrieved successfully", variant);
    }

    /**
     * Get all variants for a specific product
     */
    @Operation(
            summary = "Get all variants for a product",
            description = "Retrieve all variants/SKUs for a specific product. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProductId(
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        List<ProductVariantResponse> variants = productVariantService.getVariantsByProductId(productId);
        return success("Product variants retrieved successfully", variants);
    }

    /**
     * Update stock quantity for a specific variant
     * Example: PATCH /api/v1/product-variants/123/stock
     * Body: { "quantity": 50, "reason": "RESTOCKED" }
     */
    @Operation(
            summary = "Update variant stock",
            description = "Update the stock quantity for a specific variant. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PatchMapping("/{variantId}/stock")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantStock(
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId,
            @Valid @RequestBody StockUpdateRequest request) {
        ProductVariantResponse updated = productVariantService.updateVariantStock(variantId, request);
        return success("Stock quantity updated successfully", updated);
    }

    /**
     * Update variant active status
     * Example: PATCH /api/v1/product-variants/123/status?isActive=false
     */
    @Operation(
            summary = "Update variant status",
            description = "Update the active/inactive status of a variant. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PatchMapping("/{variantId}/status")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantStatus(
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId,
            @Parameter(description = "Active status", required = true) @RequestParam Boolean isActive) {
        ProductVariantResponse updated = productVariantService.updateVariantStatus(variantId, isActive);
        return success("Variant status updated successfully", updated);
    }

    /**
     * Update variant pricing (unit cost and/or selling price)
     * Example: PATCH /api/v1/product-variants/123/pricing?unitCost=15.50&sellingPrice=29.99
     */
    @Operation(
            summary = "Update variant pricing",
            description = "Update the unit cost and/or selling price for a variant. Requires SuperAdmin role.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @PatchMapping("/{variantId}/pricing")
    @PreAuthorize("hasRole('SuperAdmins')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantPricing(
            @Parameter(description = "Variant ID", required = true) @PathVariable Long variantId,
            @Parameter(description = "Unit cost", example = "15.50") @RequestParam(required = false) Double unitCost,
            @Parameter(description = "Selling price", example = "29.99") @RequestParam(required = false) Double sellingPrice) {

        if (unitCost == null && sellingPrice == null) {
            throw new IllegalArgumentException("At least one pricing field (unitCost or sellingPrice) must be provided");
        }

        ProductVariantResponse updated = productVariantService.updateVariantPricing(variantId, unitCost, sellingPrice);
        return success("Variant pricing updated successfully", updated);
    }
}
