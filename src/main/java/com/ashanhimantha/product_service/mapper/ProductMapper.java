package com.ashanhimantha.product_service.mapper;


import com.ashanhimantha.product_service.dto.request.ProductRequest;
import com.ashanhimantha.product_service.dto.response.AdminProductResponse;
import com.ashanhimantha.product_service.dto.response.CategoryResponse;
import com.ashanhimantha.product_service.dto.response.ProductResponse;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Creates a Spring Bean for this mapper
public interface ProductMapper {

    // --- DTO to Entity ---
    @Mapping(target = "id", ignore = true) // Ignore ID on creation
    @Mapping(target = "category", ignore = true) // We'll set this manually in the service
    @Mapping(target = "supplierId", ignore = true) // Set from JWT
    @Mapping(target = "status", ignore = true) // Set by business logic
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toProduct(ProductRequest productRequest);

    // This is the PUBLIC mapping
    ProductResponse toProductResponse(Product product);

    // THIS IS THE NEW INTERNAL MAPPING
    AdminProductResponse toAdminProductResponse(Product product);

    // MapStruct will automatically use this to map the nested Category object
    CategoryResponse toCategoryResponse(Category category);
}