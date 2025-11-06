package com.ashanhimantha.product_service.dto.response;

import com.ashanhimantha.product_service.entity.enums.Status;
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
    private List<String> sizeOptions;
    private Status status;
}
