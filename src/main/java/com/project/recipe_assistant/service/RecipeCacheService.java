package com.project.recipe_assistant.service;

import com.project.recipe_assistant.model.Recipe;

import java.util.List;

/**
 * Service đảm nhiệm phần "đắt tiền" của luồng suggestion: gọi Gemini, parse JSON, lưu Recipe.
 * <p>
 * Tách khỏi {@code RecipeService} chính vì 2 lý do:
 * <ol>
 *   <li><b>Spring Cache proxy:</b> {@code @Cacheable} chỉ hoạt động khi method được gọi
 *       từ ngoài bean (qua proxy). Self-invocation trong cùng class sẽ bypass cache.</li>
 *   <li><b>Single Responsibility:</b> {@code RecipeService} điều phối nghiệp vụ,
 *       {@code RecipeCacheService} lo phần I/O nặng - dễ test riêng từng mảng.</li>
 * </ol>
 */
public interface RecipeCacheService {

    /**
     * Trả về danh sách Recipe (đã có id) cho danh sách nguyên liệu đầu vào.
     * Cache hit -> trả ngay; cache miss -> gọi Gemini, parse, lưu DB, cache lại.
     *
     * @param normalizedIngredients danh sách nguyên liệu đã được chuẩn hoá (lowercase, sorted)
     *                              để cache key ổn định.
     */
    List<Recipe> getOrFetchRecipes(List<String> normalizedIngredients);
}
