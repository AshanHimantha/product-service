package com.ashanhimantha.product_service.mapper;

import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.dto.response.PublicProductResponse;
import com.ashanhimantha.product_service.dto.response.PublicVariantResponse;
import com.ashanhimantha.product_service.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    // --- DTO to Entity ---
    @Mapping(target = "id", ignore = true) // Ignore ID on creation
    @Mapping(target = "category", ignore = true) // We'll set this manually in the service
    @Mapping(target = "status", ignore = true) // Set by business logic
    @Mapping(target = "stock", ignore = true) // Handled by strategy pattern
    @Mapping(target = "variants", ignore = true) // Handled manually in service
    @Mapping(target = "imageUrls", ignore = true) // Handled separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toProduct(ProductRequest productRequest);

    // --- Variant DTO to Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // Set manually in service
    @Mapping(target = "isActive", ignore = true) // Default value
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductVariant toProductVariant(VariantRequest variantRequest);

    // --- Entity to DTO ---
    ProductResponse toProductResponse(Product product);

    // --- Entity to Admin DTO ---
    @Mapping(target = "totalStock", expression = "java(product.getTotalStock())")
    AdminProductResponse toAdminProductResponse(Product product);

    // --- Entity to Public DTO (for customers) ---
    PublicProductResponse toPublicProductResponse(Product product);

    // --- Variant Entity to Public Variant DTO ---
    @Mapping(target = "price", source = "sellingPrice")
    @Mapping(target = "availableStock", source = "quantity")
    PublicVariantResponse toPublicVariantResponse(ProductVariant variant);
}
