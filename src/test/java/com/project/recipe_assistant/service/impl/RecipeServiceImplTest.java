package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.dto.request.IngredientRequest;
import com.project.recipe_assistant.dto.response.RecipeSuggestionResponse;
import com.project.recipe_assistant.exception.AiServiceException;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.model.UserHistory;
import com.project.recipe_assistant.repository.RecipeRepository;
import com.project.recipe_assistant.repository.UserHistoryRepository;
import com.project.recipe_assistant.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Unit test cho {@link RecipeServiceImpl}.
 * <p>
 * Đây là test quan trọng nhất vì RecipeServiceImpl chứa logic parse JSON từ AI -
 * loại logic dễ bug nhất khi LLM thay đổi format output. Mỗi test bám một invariant
 * cụ thể của hệ thống (snapshot pattern, parse code-fence, throw đúng exception).
 * <p>
 * Dùng {@link Spy} cho ObjectMapper vì cần Jackson thực để parse JSON thật, chỉ
 * mock những thứ có side-effect (Gemini call, DB save).
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserHistoryRepository userHistoryRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private IngredientRequest request;

    @BeforeEach
    void setUp() {
        request = new IngredientRequest();
        request.setIngredients(List.of("ức gà", "tỏi"));
    }

    @Test
    @DisplayName("suggestRecipes: parse được JSON sạch và trả về DTO đầy đủ chỉ số dinh dưỡng")
    void suggestRecipes_shouldParseCleanJsonAndReturnDto() {
        String cleanJson = """
                {
                  "suggestedRecipes": [
                    {
                      "name": "Gà áp chảo",
                      "ingredients": ["ức gà", "tỏi"],
                      "instructions": ["Ướp gà", "Áp chảo"],
                      "preparationTime": 20,
                      "estimatedCalories": 350,
                      "protein": 45.0,
                      "carbs": 5.0,
                      "fat": 12.0,
                      "tags": ["Tăng cơ"]
                    }
                  ]
                }
                """;
        when(geminiService.generateContent(any())).thenReturn(cleanJson);
        // saveAll trả lại nguyên list - mô phỏng việc Mongo gắn id cho từng recipe
        when(recipeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        RecipeSuggestionResponse response = recipeService.suggestRecipes(request);

        assertThat(response.getSuggestedRecipes()).hasSize(1);
        var dto = response.getSuggestedRecipes().get(0);
        assertThat(dto.getName()).isEqualTo("Gà áp chảo");
        assertThat(dto.getProtein()).isEqualTo(45.0);
        assertThat(dto.getEstimatedCalories()).isEqualTo(350);
        assertThat(dto.getTags()).containsExactly("Tăng cơ");
    }

    @Test
    @DisplayName("suggestRecipes: strip code fence ```json...``` mà Gemini hay bọc quanh JSON")
    void suggestRecipes_shouldStripJsonCodeFence() {
        // Đây là failure mode thực tế: dù prompt yêu cầu JSON thuần,
        // Gemini đôi khi vẫn bọc code fence theo thói quen markdown
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

        RecipeSuggestionResponse response = recipeService.suggestRecipes(request);

        assertThat(response.getSuggestedRecipes()).hasSize(1);
        assertThat(response.getSuggestedRecipes().get(0).getName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("suggestRecipes: save UserHistory với suggestedRecipes EMBED (Snapshot Pattern)")
    void suggestRecipes_shouldEmbedRecipesIntoUserHistory() {
        String json = """
                {"suggestedRecipes": [
                  {"name": "A", "ingredients": [], "instructions": [], "preparationTime": 0,
                   "estimatedCalories": 0, "protein": 0, "carbs": 0, "fat": 0, "tags": []}
                ]}
                """;
        when(geminiService.generateContent(any())).thenReturn(json);
        when(recipeRepository.saveAll(anyList())).thenAnswer(inv -> {
            // Mô phỏng Mongo gán id sau khi save - quan trọng cho test snapshot
            List<Recipe> saved = inv.getArgument(0);
            saved.get(0).setId("generated-id");
            return saved;
        });

        recipeService.suggestRecipes(request);

        // Bắt object truyền vào userHistoryRepository.save để verify snapshot
        ArgumentCaptor<UserHistory> captor = ArgumentCaptor.forClass(UserHistory.class);
        verify(userHistoryRepository).save(captor.capture());

        UserHistory saved = captor.getValue();
        assertThat(saved.getRequestedIngredients()).containsExactly("ức gà", "tỏi");
        assertThat(saved.getSuggestedRecipes()).hasSize(1);
        // Đây là invariant chính của Snapshot Pattern: history phải embed Recipe ĐÃ có id,
        // tức là UserHistory chụp lại trạng thái Recipe TẠI THỜI ĐIỂM truy vấn
        assertThat(saved.getSuggestedRecipes().get(0).getId()).isEqualTo("generated-id");
    }

    @Test
    @DisplayName("suggestRecipes: ném AiServiceException khi JSON thiếu trường 'suggestedRecipes'")
    void suggestRecipes_shouldThrowAiServiceException_whenJsonStructureWrong() {
        when(geminiService.generateContent(any())).thenReturn("""
                {"wrongField": []}
                """);

        assertThatThrownBy(() -> recipeService.suggestRecipes(request))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("suggestedRecipes");

        // Đảm bảo KHÔNG save gì cả khi parse fail - tránh polluting DB
        verify(recipeRepository, times(0)).saveAll(anyList());
        verify(userHistoryRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("suggestRecipes: ném AiServiceException khi JSON malformed")
    void suggestRecipes_shouldThrowAiServiceException_whenJsonMalformed() {
        when(geminiService.generateContent(any())).thenReturn("not a json at all {{{");

        assertThatThrownBy(() -> recipeService.suggestRecipes(request))
                .isInstanceOf(AiServiceException.class);
    }

    @Test
    @DisplayName("suggestRecipes: propagate AiServiceException từ GeminiService (không nuốt lỗi)")
    void suggestRecipes_shouldPropagateGeminiException() {
        when(geminiService.generateContent(any()))
                .thenThrow(new AiServiceException("Gemini timeout"));

        assertThatThrownBy(() -> recipeService.suggestRecipes(request))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("Gemini timeout");
    }
}
