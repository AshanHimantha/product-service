package com.ashanhimantha.product_service.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic status enum for all entities (Product, Category, CategoryType, etc.)
 */
@Schema(description = "Status of the entity")
public enum Status {
    @Schema(description = "Entity is active and visible to public")
    ACTIVE,

    @Schema(description = "Entity is inactive and hidden from public")
    INACTIVE,

    @Schema(description = "Entity is in draft mode and not yet published")
    DRAFT
}
