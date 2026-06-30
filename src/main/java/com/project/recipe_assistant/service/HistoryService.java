package com.project.recipe_assistant.service;

import com.project.recipe_assistant.dto.response.UserHistoryResponse;

import java.util.List;

/**
 * Service truy vấn lịch sử các phiên gợi ý món ăn của người dùng.
 * Đọc {@code UserHistory} (đã embed snapshot Recipe) và map sang DTO.
 */
public interface HistoryService {

    /** Trả về tất cả lịch sử, mới nhất trước. */
    List<UserHistoryResponse> getAllHistory();

    /** Trả về 1 phiên lịch sử theo id; ném ResourceNotFoundException nếu không có. */
    UserHistoryResponse getHistoryById(String id);
}
