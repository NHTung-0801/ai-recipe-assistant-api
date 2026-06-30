package com.project.recipe_assistant.service.impl;

import com.project.recipe_assistant.constant.AppConstants;
import com.project.recipe_assistant.exception.AiServiceException;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.repository.RecipeRepository;
import com.project.recipe_assistant.service.GeminiService;
import com.project.recipe_assistant.service.RecipeCacheService;
import com.project.recipe_assistant.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Triển khai logic cache + I/O nặng cho luồng gợi ý món ăn.
 * <p>
 * {@code @Cacheable} dùng key = chính {@code normalizedIngredients} (List<String>),
 * Spring sẽ tính hashCode/equals của list để làm cache key. Caller phải đảm bảo
 * list đã được normalize (lowercase + sorted) trước khi gọi để cache hit ổn định.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeCacheServiceImpl implements RecipeCacheService {

    private final GeminiService geminiService;
    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = AppConstants.CACHE_RECIPE_SUGGESTIONS, key = "#normalizedIngredients")
    public List<Recipe> getOrFetchRecipes(List<String> normalizedIngredients) {
        // Log này CHỈ xuất hiện khi cache miss - dùng để verify cache đang hoạt động
        log.info("Cache MISS - gọi Gemini cho ingredients: {}", normalizedIngredients);

        String prompt = PromptBuilder.buildRecipeRequestPrompt(normalizedIngredients);
        String aiRawJson = geminiService.generateContent(prompt);
        log.debug("Gemini raw response: {}", aiRawJson);

        List<Recipe> parsedRecipes = parseRecipesFromAiResponse(aiRawJson);

        // Persist từng Recipe (Mongo cấp _id). Phải save TRƯỚC khi cache lưu,
        // để Recipe trong cache có id - tránh client thấy id null khi cache hit.
        return recipeRepository.saveAll(parsedRecipes);
    }

    /**
     * Parse chuỗi JSON do Gemini trả về thành {@code List<Recipe>}.
     * <p>
     * Có 2 vấn đề thực tế khi làm việc với LLM cần xử lý:
     * <ul>
     *   <li><b>Code fence:</b> Gemini hay bọc JSON trong khối <code>```json ... ```</code>
     *       dù prompt đã yêu cầu trả về JSON thuần. Phải strip trước khi parse.</li>
     *   <li><b>Nested structure:</b> Theo prompt, JSON có cấu trúc
     *       <code>{"suggestedRecipes": [...]}</code> — cần lấy đúng nhánh con,
     *       không deserialize trực tiếp ra <code>List&lt;Recipe&gt;</code>.</li>
     * </ul>
     */
    private List<Recipe> parseRecipesFromAiResponse(String aiRawJson) {
        try {
            String cleaned = stripJsonCodeFence(aiRawJson);

            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode recipesNode = root.path("suggestedRecipes");

            if (recipesNode.isMissingNode() || !recipesNode.isArray()) {
                throw new AiServiceException(
                        "JSON trả về không chứa trường 'suggestedRecipes' dạng mảng. Raw: " + aiRawJson);
            }

            return objectMapper.readerForListOf(Recipe.class).readValue(recipesNode);

        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Lỗi parse JSON từ AI. Raw: {}", aiRawJson, ex);
            throw new AiServiceException("Dữ liệu AI trả về không đúng định dạng JSON kỳ vọng", ex);
        }
    }

    /**
     * Loại bỏ ký hiệu markdown code fence (```json ... ``` hoặc ``` ... ```) mà LLM hay thêm vào.
     * Nếu chuỗi không có code fence thì trả nguyên giá trị (đã trim).
     */
    private String stripJsonCodeFence(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}
