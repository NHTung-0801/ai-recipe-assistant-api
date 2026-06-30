package com.project.recipe_assistant.controller;

import com.project.recipe_assistant.constant.AppConstants;
import com.project.recipe_assistant.dto.request.IngredientRequest;
import com.project.recipe_assistant.dto.response.ApiResponse;
import com.project.recipe_assistant.dto.response.RecipeSuggestionResponse;
import com.project.recipe_assistant.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint cung cấp tính năng gợi ý công thức nấu ăn từ nguyên liệu.
 * Controller giữ vai trò mỏng (thin layer): chỉ nhận request, validate, ủy quyền cho service.
 */
@RestController
@RequestMapping(AppConstants.API_PREFIX_V1 + "/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipe", description = "API gợi ý món ăn dựa trên nguyên liệu sẵn có")
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * POST /api/v1/recipes/suggest
     * <p>
     * Annotation {@code @Valid} kích hoạt validation trên {@link IngredientRequest};
     * nếu vi phạm, Spring sẽ ném {@code MethodArgumentNotValidException}
     * và {@code GlobalExceptionHandler} xử lý — controller không cần if/else.
     */
    @Operation(
            summary = "Gợi ý 2-3 món ăn từ danh sách nguyên liệu",
            description = "Gửi danh sách nguyên liệu sẵn có, AI sẽ phân tích và đề xuất các công thức kèm chỉ số dinh dưỡng (calories, protein, carbs, fat) và thời gian chuẩn bị."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Trả về danh sách công thức đề xuất"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Danh sách nguyên liệu rỗng hoặc không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503", description = "Gemini AI service không khả dụng")
    })
    @PostMapping("/suggest")
    public ResponseEntity<ApiResponse<RecipeSuggestionResponse>> suggestRecipes(
            @Valid @RequestBody IngredientRequest request) {

        RecipeSuggestionResponse data = recipeService.suggestRecipes(request);

        ApiResponse<RecipeSuggestionResponse> response = ApiResponse.<RecipeSuggestionResponse>builder()
                .status(HttpStatus.OK.value())
                .message(AppConstants.MSG_SUCCESS)
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }
}
