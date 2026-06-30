package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.exception.AiServiceException;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.repository.RecipeRepository;
import com.project.recipe_assistant.service.GeminiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link RecipeCacheServiceImpl} - tập trung vào logic parse JSON.
 * <p>
 * Cache behavior (annotation {@code @Cacheable}) KHÔNG được test ở đây vì cần Spring proxy.
 * Unit test này chỉ gọi method trực tiếp, cache không kích hoạt - đúng ý đồ:
 * mỗi tầng test một thứ.
 * <p>
 * Dùng {@link Spy} cho ObjectMapper vì cần Jackson thực để parse JSON,
 * chỉ mock những thứ có side-effect (Gemini call, DB save).
 */
@ExtendWith(MockitoExtension.class)
class RecipeCacheServiceImplTest {

    @Mock private GeminiService geminiService;
    @Mock private RecipeRepository recipeRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RecipeCacheServiceImpl cacheService;

    private final List<String> ingredients = List.of("ức gà", "tỏi");

    @Test
    @DisplayName("getOrFetchRecipes: parse được JSON sạch và trả về Recipe đầy đủ chỉ số")
    void getOrFetchRecipes_shouldParseCleanJson() {
        String cleanJson = """
                {
                  "suggestedRecipes": [
                    {
                      "name": "Gà áp chảo", "ingredients": ["ức gà", "tỏi"],
                      "instructions": ["Ướp gà", "Áp chảo"], "preparationTime": 20,
                      "estimatedCalories": 350, "protein": 45.0,
                      "carbs": 5.0, "fat": 12.0, "tags": ["Tăng cơ"]
                    }
                  ]
                }
                """;
        when(geminiService.generateContent(any())).thenReturn(cleanJson);
        when(recipeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Recipe> result = cacheService.getOrFetchRecipes(ingredients);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Gà áp chảo");
        assertThat(result.get(0).getProtein()).isEqualTo(45.0);
        assertThat(result.get(0).getTags()).containsExactly("Tăng cơ");
    }

    @Test
    @DisplayName("getOrFetchRecipes: strip code fence ```json...``` mà Gemini hay bọc quanh JSON")
    void getOrFetchRecipes_shouldStripJsonCodeFence() {
        String fencedJson = """
                ```json
                {
                  "suggestedRecipes": [
                    {"name": "Test", "ingredients": [], "instructions": [], "preparationTime": 0,
                     "estimatedCalories": 100, "protein": 1.0, "carbs": 1.0, "fat": 1.0, "tags": []}
                  ]
                }
                ```
                """;
        when(geminiService.generateContent(any())).thenReturn(fencedJson);
        when(recipeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Recipe> result = cacheService.getOrFetchRecipes(ingredients);

        assertThat(result.get(0).getName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("getOrFetchRecipes: ném AiServiceException khi JSON thiếu trường 'suggestedRecipes'")
    void getOrFetchRecipes_shouldThrow_whenJsonStructureWrong() {
        when(geminiService.generateContent(any())).thenReturn("""
                {"wrongField": []}
                """);

        assertThatThrownBy(() -> cacheService.getOrFetchRecipes(ingredients))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("suggestedRecipes");

        verify(recipeRepository, times(0)).saveAll(anyList());
    }

    @Test
    @DisplayName("getOrFetchRecipes: ném AiServiceException khi JSON malformed")
    void getOrFetchRecipes_shouldThrow_whenJsonMalformed() {
        when(geminiService.generateContent(any())).thenReturn("not a json at all {{{");

        assertThatThrownBy(() -> cacheService.getOrFetchRecipes(ingredients))
                .isInstanceOf(AiServiceException.class);
    }

    @Test
    @DisplayName("getOrFetchRecipes: propagate AiServiceException từ GeminiService")
    void getOrFetchRecipes_shouldPropagateGeminiException() {
        when(geminiService.generateContent(any()))
                .thenThrow(new AiServiceException("Gemini timeout"));

        assertThatThrownBy(() -> cacheService.getOrFetchRecipes(ingredients))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("Gemini timeout");
    }
}
