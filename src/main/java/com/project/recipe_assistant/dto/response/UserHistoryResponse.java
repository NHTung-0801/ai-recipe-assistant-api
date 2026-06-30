package com.project.recipe_assistant.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về cho client khi truy vấn lịch sử tìm kiếm.
 * Tách khỏi entity {@code UserHistory} để không leak chi tiết MongoDB ra ngoài
 * và để có thể tiến hóa response schema độc lập với schema lưu trữ.
 */
@Data
@Builder
public class UserHistoryResponse {

    private String id;
    private List<String> requestedIngredients;
    private List<RecipeResponse> suggestedRecipes;
    private LocalDateTime searchTime;
}
