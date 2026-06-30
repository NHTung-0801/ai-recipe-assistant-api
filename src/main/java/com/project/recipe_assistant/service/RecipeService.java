package com.project.recipe_assistant.service;

import com.project.recipe_assistant.dto.request.IngredientRequest;
import com.project.recipe_assistant.dto.response.RecipeSuggestionResponse;

/**
 * Service nghiệp vụ chính của ứng dụng.
 * Orchestrator: phối hợp {@link GeminiService} (lấy gợi ý từ AI),
 * Repository (persist Recipe + UserHistory) và mapper (Entity -> DTO).
 */
public interface RecipeService {

    /**
     * Đề xuất danh sách công thức nấu ăn dựa trên nguyên liệu người dùng cung cấp.
     * Quy trình: build prompt -> gọi AI -> parse JSON -> lưu DB (Recipe + Snapshot UserHistory)
     * -> map sang DTO trả về.
     */
    RecipeSuggestionResponse suggestRecipes(IngredientRequest request);
}
