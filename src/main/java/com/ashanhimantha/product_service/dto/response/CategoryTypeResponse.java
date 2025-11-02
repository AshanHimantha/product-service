package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.SizingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTypeResponse {
    private Long id;
    private String name;
    private SizingType sizingType;
    private List<String> sizeOptions;
}
