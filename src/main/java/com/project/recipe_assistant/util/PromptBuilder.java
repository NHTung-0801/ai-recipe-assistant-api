package com.project.recipe_assistant.util;

import com.project.recipe_assistant.constant.AppConstants;

import java.util.List;

public class PromptBuilder {

    private PromptBuilder() {
    }

    public static String buildRecipeRequestPrompt(List<String> ingredients) {
        String ingredientsList = String.join(", ", ingredients);

        return AppConstants.GEMINI_SYSTEM_PROMPT +
                "\n\nHere is the list of available ingredients: " +
                ingredientsList +
                "\n\nPlease generate the recipes based ONLY on these ingredients and strictly follow the required JSON structure.";
    }
}