package com.project.recipe_assistant.service.impl;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.project.recipe_assistant.dto.request.IngredientRequest;
import com.project.recipe_assistant.dto.response.RecipeResponse;
import com.project.recipe_assistant.dto.response.RecipeSuggestionResponse;
import com.project.recipe_assistant.exception.AiServiceException;
import com.project.recipe_assistant.model.Recipe;
import com.project.recipe_assistant.model.UserHistory;
import com.project.recipe_assistant.repository.RecipeRepository;
import com.project.recipe_assistant.repository.UserHistoryRepository;
import com.project.recipe_assistant.service.GeminiService;
import com.project.recipe_assistant.service.RecipeService;
import com.project.recipe_assistant.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Triển khai logic nghiệp vụ chính: chỉ huy AI -> parse -> persist -> map sang DTO.
 * Tách rời {@link GeminiService} để service này có thể được test với mock dễ dàng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final GeminiService geminiService;
    private final RecipeRepository recipeRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public RecipeSuggestionResponse suggestRecipes(IngredientRequest request) {
        List<String> ingredients = request.getIngredients();

        // 1. Build prompt và gọi Gemini lấy về chuỗi JSON
        String prompt = PromptBuilder.buildRecipeRequestPrompt(ingredients);
        String aiRawJson = geminiService.generateContent(prompt);
        log.debug("Gemini raw response: {}", aiRawJson);

        // 2. Parse JSON -> List<Recipe>
        List<Recipe> parsedRecipes = parseRecipesFromAiResponse(aiRawJson);

        // 3. Persist từng Recipe (mỗi recipe sẽ được Mongo cấp _id sau khi save).
        //    saveAll trả về list các Recipe đã được gắn id — phải dùng list này khi build snapshot.
        List<Recipe> savedRecipes = recipeRepository.saveAll(parsedRecipes);

        // 4. Snapshot Pattern: nhúng nguyên list Recipe đã save vào UserHistory.
        //    searchTime sẽ được @CreatedDate (kết hợp @EnableMongoAuditing) tự động sinh khi save.
        UserHistory history = UserHistory.builder()
                .requestedIngredients(ingredients)
                .suggestedRecipes(savedRecipes)
                .build();
        userHistoryRepository.save(history);

        // 5. Map Entity -> DTO trước khi trả về Controller
        return mapToSuggestionResponse(savedRecipes);
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

            // treeToValue dùng TypeReference để giữ generic type khi deserialize sang List<Recipe>
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
            // Cắt bỏ dòng "```json" hoặc "```" ở đầu và "```" ở cuối
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
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
