package com.ashanhimantha.product_service.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * A simplified, client-friendly DTO for paginated data.
 * @param <T> The type of the content in the list.
 */
@Data
@Schema(description = "Paginated response wrapper")
public class PaginatedResponse<T> {

    @Schema(description = "List of items in the current page")
    private List<T> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int currentPage;

    @Schema(description = "Number of items per page", example = "20")
    private int pageSize;

    @Schema(description = "Total number of items across all pages", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Whether this is the last page", example = "false")
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