package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.dto.response.UserHistoryResponse;
import com.project.recipe_assistant.exception.ResourceNotFoundException;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.model.UserHistory;
import com.project.recipe_assistant.repository.UserHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link HistoryServiceImpl}.
 * <p>
 * Mock repository - test riêng logic mapping Entity -> DTO và xử lý not-found.
 * KHÔNG cần Spring context vì service không có magic Spring nào (chỉ inject thường).
 */
@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

    @Mock
    private UserHistoryRepository userHistoryRepository;

    @InjectMocks
    private HistoryServiceImpl historyService;

    @Test
    @DisplayName("getAllHistory: map đúng toàn bộ trường từ UserHistory sang DTO")
    void getAllHistory_shouldMapAllFieldsCorrectly() {
        Recipe recipe = Recipe.builder()
                .id("recipe-1").name("Gà nướng").preparationTime(30)
                .estimatedCalories(400).protein(45.0).carbs(10.0).fat(15.0)
                .build();
        UserHistory history = UserHistory.builder()
                .id("hist-1")
                .requestedIngredients(List.of("gà", "muối"))
                .suggestedRecipes(List.of(recipe))
                .searchTime(LocalDateTime.of(2026, 6, 30, 10, 0))
                .build();
        when(userHistoryRepository.findAllByOrderBySearchTimeDesc()).thenReturn(List.of(history));

        List<UserHistoryResponse> result = historyService.getAllHistory();

        assertThat(result).hasSize(1);
        UserHistoryResponse dto = result.get(0);
        assertThat(dto.getId()).isEqualTo("hist-1");
        assertThat(dto.getRequestedIngredients()).containsExactly("gà", "muối");
        assertThat(dto.getSuggestedRecipes()).hasSize(1);
        assertThat(dto.getSuggestedRecipes().get(0).getName()).isEqualTo("Gà nướng");
        assertThat(dto.getSuggestedRecipes().get(0).getProtein()).isEqualTo(45.0);
    }

    @Test
    @DisplayName("getAllHistory: list rỗng khi repository trả empty")
    void getAllHistory_shouldReturnEmptyList_whenNoHistory() {
        when(userHistoryRepository.findAllByOrderBySearchTimeDesc()).thenReturn(List.of());

        assertThat(historyService.getAllHistory()).isEmpty();
    }

    @Test
    @DisplayName("getAllHistory: xử lý null suggestedRecipes mà không NPE")
    void getAllHistory_shouldHandleNullSuggestedRecipes() {
        // Edge case: UserHistory cũ trong DB có suggestedRecipes = null
        UserHistory history = UserHistory.builder()
                .id("hist-1").requestedIngredients(List.of("a"))
                .suggestedRecipes(null)
                .build();
        when(userHistoryRepository.findAllByOrderBySearchTimeDesc()).thenReturn(List.of(history));

        List<UserHistoryResponse> result = historyService.getAllHistory();

        assertThat(result.get(0).getSuggestedRecipes()).isEmpty();
    }

    @Test
    @DisplayName("getHistoryById: trả về DTO khi tìm thấy")
    void getHistoryById_shouldReturnDto_whenFound() {
        UserHistory history = UserHistory.builder().id("h-1").requestedIngredients(List.of("x")).build();
        when(userHistoryRepository.findById("h-1")).thenReturn(Optional.of(history));

        UserHistoryResponse result = historyService.getHistoryById("h-1");

        assertThat(result.getId()).isEqualTo("h-1");
    }

    @Test
    @DisplayName("getHistoryById: ném ResourceNotFoundException khi không tìm thấy")
    void getHistoryById_shouldThrow_whenNotFound() {
        when(userHistoryRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.getHistoryById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
