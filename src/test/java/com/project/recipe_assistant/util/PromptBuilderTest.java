package com.project.recipe_assistant.util;

import com.project.recipe_assistant.constant.AppConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test cho {@link PromptBuilder}.
 * <p>
 * PromptBuilder là pure function (không có dependency, không có state) — đây là loại
 * code dễ test nhất và đáng test nhất vì lỗi ở đây ảnh hưởng trực tiếp output của AI.
 */
class PromptBuilderTest {

    @Test
    @DisplayName("Prompt phải chứa system prompt gốc + tất cả nguyên liệu")
    void buildRecipeRequestPrompt_shouldContainSystemPromptAndAllIngredients() {
        List<String> ingredients = List.of("ức gà", "bông cải xanh", "tỏi");

        String result = PromptBuilder.buildRecipeRequestPrompt(ingredients);

        assertThat(result)
                .contains(AppConstants.GEMINI_SYSTEM_PROMPT)
                .contains("ức gà")
                .contains("bông cải xanh")
                .contains("tỏi");
    }

    @Test
    @DisplayName("Nguyên liệu phải được nối bằng dấu phẩy và space")
    void buildRecipeRequestPrompt_shouldJoinIngredientsWithCommaSpace() {
        List<String> ingredients = List.of("a", "b", "c");

        String result = PromptBuilder.buildRecipeRequestPrompt(ingredients);

        assertThat(result).contains("a, b, c");
    }

    @Test
    @DisplayName("Prompt phải có instruction ràng buộc JSON output")
    void buildRecipeRequestPrompt_shouldEnforceJsonStructure() {
        String result = PromptBuilder.buildRecipeRequestPrompt(List.of("x"));

        // Đảm bảo prompt nhắc Gemini phải dùng JSON - đây là invariant quan trọng,
        // nếu dòng này bị xoá thì AI có thể trả markdown/plain text khiến parse fail
        assertThat(result).containsIgnoringCase("JSON");
    }
}
