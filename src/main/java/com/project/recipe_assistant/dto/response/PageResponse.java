package com.project.recipe_assistant.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * DTO chuẩn cho response phân trang.
 * <p>
 * Tách khỏi {@link org.springframework.data.domain.Page} của Spring vì 2 lý do:
 * <ul>
 *   <li>JSON serialization của Page có nhiều trường thừa và format không ổn định
 *       qua các version Spring - dễ break client.</li>
 *   <li>Frontend chỉ cần các trường cốt lõi: content + thông tin paging.</li>
 * </ul>
 */
@Data
@Builder
public class PageResponse<T> {

    private List<T> content;     // Dữ liệu trang hiện tại
    private int page;            // Số trang (0-based)
    private int size;            // Số phần tử trên 1 trang
    private long totalElements;  // Tổng số record trong DB
    private int totalPages;      // Tổng số trang
    private boolean first;       // Có phải trang đầu không
    private boolean last;        // Có phải trang cuối không

    /**
     * Factory method: convert Page<E> của Spring Data sang PageResponse<T> với mapper.
     * Pattern này tránh lặp code map ở mỗi service có pagination.
     */
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return PageResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
