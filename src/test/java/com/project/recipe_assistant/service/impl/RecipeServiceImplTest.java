package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.dto.request.IngredientRequest;
import com.project.recipe_assistant.dto.response.RecipeSuggestionResponse;
import com.project.recipe_assistant.exception.AiServiceException;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.model.UserHistory;
import com.project.recipe_assistant.repository.UserHistoryRepository;
import com.project.recipe_assistant.service.RecipeCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link RecipeServiceImpl} sau khi refactor caching.
 * <p>
 * Service này giờ chỉ làm 3 việc: normalize ingredients, gọi cache service, save UserHistory.
 * Logic parse JSON / gọi Gemini đã chuyển sang {@link RecipeCacheServiceImplTest}.
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeCacheService recipeCacheService;

    @Mock
    private UserHistoryRepository userHistoryRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private IngredientRequest request;

    @BeforeEach
    void setUp() {
        request = new IngredientRequest();
        request.setIngredients(List.of("Ức Gà", "tỏi"));
    }

    @Test
    @DisplayName("suggestRecipes: chuẩn hoá ingredients (lowercase + sort) trước khi gọi cache")
    void suggestRecipes_shouldNormalizeIngredientsForCacheKey() {
        when(recipeCacheService.getOrFetchRecipes(anyList()))
                .thenReturn(List.of(buildRecipe("Gà nướng")));

        recipeService.suggestRecipes(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(recipeCacheService).getOrFetchRecipes(captor.capture());

        // Invariant cache: cùng combo nguyên liệu phải tạo cùng key bất kể case và thứ tự
        assertThat(captor.getValue()).containsExactly("tỏi", "ức gà");
    }

    @Test
    @DisplayName("suggestRecipes: save UserHistory với suggestedRecipes EMBED (Snapshot Pattern)")
    void suggestRecipes_shouldEmbedRecipesIntoUserHistory() {
        Recipe recipe = buildRecipe("Gà");
        recipe.setId("rec-1");
        when(recipeCacheService.getOrFetchRecipes(anyList())).thenReturn(List.of(recipe));

        recipeService.suggestRecipes(request);

        ArgumentCaptor<UserHistory> captor = ArgumentCaptor.forClass(UserHistory.class);
        verify(userHistoryRepository).save(captor.capture());

        UserHistory saved = captor.getValue();
        // History phải lưu nguyên liệu GỐC từ user (không phải bản đã normalize) - audit chính xác
        assertThat(saved.getRequestedIngredients()).containsExactly("Ức Gà", "tỏi");
        assertThat(saved.getSuggestedRecipes()).hasSize(1);
        assertThat(saved.getSuggestedRecipes().get(0).getId()).isEqualTo("rec-1");
    }

    @Test
    @DisplayName("suggestRecipes: trả về DTO đúng các trường dinh dưỡng")
    void suggestRecipes_shouldMapNutritionFieldsToDto() {
        Recipe recipe = Recipe.builder()
                .id("r-1").name("Gà").protein(45.0).carbs(5.0).fat(12.0)
                .estimatedCalories(350).preparationTime(20)
                .build();
        when(recipeCacheService.getOrFetchRecipes(anyList())).thenReturn(List.of(recipe));

        RecipeSuggestionResponse response = recipeService.suggestRecipes(request);

        var dto = response.getSuggestedRecipes().get(0);
        assertThat(dto.getName()).isEqualTo("Gà");
        assertThat(dto.getProtein()).isEqualTo(45.0);
        assertThat(dto.getEstimatedCalories()).isEqualTo(350);
    }

    @Test
    @DisplayName("suggestRecipes: propagate exception từ cache service (không nuốt lỗi)")
    void suggestRecipes_shouldPropagateCacheServiceException() {
        when(recipeCacheService.getOrFetchRecipes(anyList()))
                .thenThrow(new AiServiceException("Gemini failed"));

        assertThatThrownBy(() -> recipeService.suggestRecipes(request))
                .isInstanceOf(AiServiceException.class);

        // Khi cache service fail, KHÔNG save history rác
        verify(userHistoryRepository, times(0)).save(any());
    }

    private Recipe buildRecipe(String name) {
        return Recipe.builder().name(name).build();
    }
}
