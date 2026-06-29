package com.project.recipe_assistant.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeResponse {

    private String id;
    private String name;
    private List<String> ingredients;
    private List<String> instructions;
    private int preparationTime;

    // Thông tin dinh dưỡng
    private int estimatedCalories;
    private double protein;
    private double carbs;
    private double fat;

    private List<String> tags;
}