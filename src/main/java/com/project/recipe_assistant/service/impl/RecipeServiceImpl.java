package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.dto.request.IngredientRequest;
import com.project.recipe_assistant.dto.response.RecipeResponse;
import com.project.recipe_assistant.dto.response.RecipeSuggestionResponse;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.model.UserHistory;
import com.project.recipe_assistant.repository.UserHistoryRepository;
import com.project.recipe_assistant.service.RecipeCacheService;
import com.project.recipe_assistant.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Triển khai logic nghiệp vụ chính: chuẩn hoá ingredients -> lấy Recipes (qua cache) ->
 * persist UserHistory snapshot -> map sang DTO.
 * <p>
 * Phần "đắt tiền" (Gemini call + parse JSON + save Recipe) đã được tách sang
 * {@link RecipeCacheService} để hưởng lợi từ {@code @Cacheable}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeCacheService recipeCacheService;
    private final UserHistoryRepository userHistoryRepository;

    @Override
    public RecipeSuggestionResponse suggestRecipes(IngredientRequest request) {
        // 1. Chuẩn hoá ingredients để cache key ổn định:
        //    - lowercase: "Ức Gà" và "ức gà" phải hit cùng cache entry
        //    - sorted: thứ tự nhập không quan trọng về ngữ nghĩa (gà+tỏi == tỏi+gà)
        List<String> normalized = request.getIngredients().stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .sorted(Comparator.naturalOrder())
                .toList();

        // 2. Lấy Recipes - qua cache. Cache miss sẽ tự gọi Gemini + parse + save.
        List<Recipe> recipes = recipeCacheService.getOrFetchRecipes(normalized);

        // 3. Luôn save UserHistory mới (mỗi lần search là 1 phiên, dù cache hit hay miss).
        //    Snapshot Pattern: embed Recipes đã có id vào history.
        UserHistory history = UserHistory.builder()
                .requestedIngredients(request.getIngredients()) // Lưu nguyên liệu GỐC từ user
                .suggestedRecipes(recipes)
                .build();
        userHistoryRepository.save(history);

        return mapToSuggestionResponse(recipes);
    }

    private RecipeSuggestionResponse mapToSuggestionResponse(List<Recipe> recipes) {
        List<RecipeResponse> items = recipes.stream()
                .map(this::toRecipeResponse)
                .toList();
        return RecipeSuggestionResponse.builder()
                .suggestedRecipes(items)
                .build();
    }

    private RecipeResponse toRecipeResponse(Recipe r) {
        return RecipeResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .ingredients(r.getIngredients())
                .instructions(r.getInstructions())
                .preparationTime(r.getPreparationTime())
                .estimatedCalories(r.getEstimatedCalories())
                .protein(r.getProtein())
                .carbs(r.getCarbs())
                .fat(r.getFat())
                .tags(r.getTags())
                .build();
    }
}
