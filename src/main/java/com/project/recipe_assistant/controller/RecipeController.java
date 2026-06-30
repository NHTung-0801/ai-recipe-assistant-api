package com.project.recipe_assistant.controller;

import com.project.recipe_assistant.constant.AppConstants;
import com.project.recipe_assistant.dto.request.IngredientRequest;
import com.project.recipe_assistant.dto.response.ApiResponse;
import com.project.recipe_assistant.dto.response.RecipeSuggestionResponse;
import com.project.recipe_assistant.service.RecipeService;
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
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * POST /api/v1/recipes/suggest
     * <p>
     * Annotation {@code @Valid} kích hoạt validation trên {@link IngredientRequest};
     * nếu vi phạm, Spring sẽ ném {@code MethodArgumentNotValidException}
     * và {@code GlobalExceptionHandler} xử lý — controller không cần if/else.
     */
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
