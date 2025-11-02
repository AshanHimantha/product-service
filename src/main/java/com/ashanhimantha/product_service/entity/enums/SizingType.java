package com.ashanhimantha.product_service.entity.enums;

/**
 * Defines the type of sizing/measurement system for a category
 */
public enum SizingType {
    CLOTHING_LETTER,    // S, M, L, XL, XXL, etc.
    NUMERIC_SIZE,       // 28, 30, 32, 34, etc. (for pants, waist sizes)
    SHOE_SIZE,          // 7, 8, 9, 10, etc.
    LENGTH_CM,          // cm, m, mm
    LENGTH_INCH,        // inch, ft
    VOLUME_ML,          // ml, l
    WEIGHT_G,           // g, kg, mg
    NONE                // No specific sizing (e.g., accessories, one-size items)
}

