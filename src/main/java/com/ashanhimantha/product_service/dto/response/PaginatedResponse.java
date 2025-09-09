package com.ashanhimantha.product_service.dto.response;


import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * A simplified, client-friendly DTO for paginated data.
 * @param <T> The type of the content in the list.
 */
@Data
public class PaginatedResponse<T> {

    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;

    // A convenient constructor to map from a Spring Data Page object
    public PaginatedResponse(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.isLast = page.isLast();
    }
}