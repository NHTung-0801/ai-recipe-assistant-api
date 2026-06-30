package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.dto.response.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

    @Mock
    private UserHistoryRepository userHistoryRepository;

    @InjectMocks
    private HistoryServiceImpl historyService;

    @Test
    @DisplayName("getHistory: map đúng Page<UserHistory> sang PageResponse<DTO> với metadata đầy đủ")
    void getHistory_shouldMapPageWithMetadata() {
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

        Pageable pageable = PageRequest.of(0, 10, Sort.by("searchTime").descending());
        // PageImpl với totalElements=25 để verify metadata (totalPages, last) tính đúng
        Page<UserHistory> page = new PageImpl<>(List.of(history), pageable, 25);
        when(userHistoryRepository.findBy(pageable)).thenReturn(page);

        PageResponse<UserHistoryResponse> result = historyService.getHistory(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("hist-1");
        assertThat(result.getContent().get(0).getSuggestedRecipes().get(0).getName()).isEqualTo("Gà nướng");
        // Verify metadata pagination
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3); // ceil(25/10)
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    @DisplayName("getHistory: trả về page rỗng khi không có dữ liệu")
    void getHistory_shouldReturnEmptyPage_whenNoData() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userHistoryRepository.findBy(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PageResponse<UserHistoryResponse> result = historyService.getHistory(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("getHistory: xử lý null suggestedRecipes mà không NPE")
    void getHistory_shouldHandleNullSuggestedRecipes() {
        // Edge case: UserHistory cũ trong DB có suggestedRecipes = null
        UserHistory history = UserHistory.builder()
                .id("hist-1").requestedIngredients(List.of("a"))
                .suggestedRecipes(null)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        when(userHistoryRepository.findBy(pageable))
                .thenReturn(new PageImpl<>(List.of(history), pageable, 1));

        PageResponse<UserHistoryResponse> result = historyService.getHistory(pageable);

        assertThat(result.getContent().get(0).getSuggestedRecipes()).isEmpty();
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

    @Test
    @DisplayName("deleteHistory: xóa thành công khi id tồn tại")
    void deleteHistory_shouldDelete_whenExists() {
        when(userHistoryRepository.existsById("h-1")).thenReturn(true);

        historyService.deleteHistory("h-1");

        verify(userHistoryRepository).deleteById("h-1");
    }

    @Test
    @DisplayName("deleteHistory: ném ResourceNotFoundException khi id không tồn tại (không silent no-op)")
    void deleteHistory_shouldThrow_whenNotFound() {
        when(userHistoryRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> historyService.deleteHistory("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        // Verify KHÔNG gọi deleteById khi không tồn tại
        verify(userHistoryRepository, never()).deleteById(anyString());
    }
}
