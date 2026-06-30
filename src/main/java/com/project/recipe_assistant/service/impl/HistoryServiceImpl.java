package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.dto.response.PageResponse;
import com.project.recipe_assistant.dto.response.RecipeResponse;
import com.project.recipe_assistant.dto.response.UserHistoryResponse;
import com.project.recipe_assistant.exception.ResourceNotFoundException;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.model.UserHistory;
import com.project.recipe_assistant.repository.UserHistoryRepository;
import com.project.recipe_assistant.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final UserHistoryRepository userHistoryRepository;

    @Override
    public PageResponse<UserHistoryResponse> getHistory(Pageable pageable) {
        return PageResponse.from(
                userHistoryRepository.findBy(pageable),
                this::toResponse);
    }

    @Override
    public UserHistoryResponse getHistoryById(String id) {
        UserHistory history = userHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lịch sử với id: " + id));
        return toResponse(history);
    }

    @Override
    public void deleteHistory(String id) {
        // Check tồn tại trước khi xóa - để trả 404 thay vì silently no-op
        if (!userHistoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch sử với id: " + id);
        }
        userHistoryRepository.deleteById(id);
    }

    private UserHistoryResponse toResponse(UserHistory h) {
        List<Recipe> recipes = h.getSuggestedRecipes() != null
                ? h.getSuggestedRecipes()
                : Collections.emptyList();

        List<RecipeResponse> mapped = recipes.stream()
                .map(this::toRecipeResponse)
                .toList();

        return UserHistoryResponse.builder()
                .id(h.getId())
                .requestedIngredients(h.getRequestedIngredients())
                .suggestedRecipes(mapped)
                .searchTime(h.getSearchTime())
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
