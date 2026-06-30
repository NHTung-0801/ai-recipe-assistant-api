package com.project.recipe_assistant.service;

import com.project.recipe_assistant.dto.response.PageResponse;
import com.project.recipe_assistant.dto.response.UserHistoryResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service truy vấn lịch sử các phiên gợi ý món ăn của người dùng.
 * Đọc {@code UserHistory} (đã embed snapshot Recipe) và map sang DTO.
 */
public interface HistoryService {

    /** Trả về lịch sử có phân trang, mặc định sort theo searchTime desc. */
    PageResponse<UserHistoryResponse> getHistory(Pageable pageable);

    /** Trả về 1 phiên lịch sử theo id; ném ResourceNotFoundException nếu không có. */
    UserHistoryResponse getHistoryById(String id);

    /** Xóa 1 phiên lịch sử theo id; ném ResourceNotFoundException nếu không có. */
    void deleteHistory(String id);
}
