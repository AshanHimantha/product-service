package com.ashanhimantha.product_service.service;

import com.ashanhimantha.product_service.dto.request.StockUpdateRequest;
import com.ashanhimantha.product_service.dto.request.VariantRequest;
import com.ashanhimantha.product_service.dto.request.VariantUpdateRequest;
import com.ashanhimantha.product_service.dto.response.ProductVariantResponse;

import java.util.List;

public interface ProductVariantService {

    ProductVariantResponse createVariant(Long productId, VariantRequest request);
    ProductVariantResponse getVariantById(Long variantId);
    List<ProductVariantResponse> getVariantsByProductId(Long productId);
    ProductVariantResponse updateVariant(Long variantId, VariantUpdateRequest request);

}
