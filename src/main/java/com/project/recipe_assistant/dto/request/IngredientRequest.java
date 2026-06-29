package com.project.recipe_assistant.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class IngredientRequest {

    @NotEmpty(message = "Danh sách nguyên liệu không được để trống")
    private List<String> ingredients;

}