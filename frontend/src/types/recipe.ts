/**
 * Recipe response từ backend.
 * Match RecipeResponse.java - chứa cả thông tin dinh dưỡng (macros).
 */
export interface Recipe {
  id: string
  name: string
  ingredients: string[]
  instructions: string[]
  preparationTime: number
  // Macros
  estimatedCalories: number
  protein: number
  carbs: number
  fat: number
  tags: string[]
}

/** Request body cho POST /api/v1/recipes/suggest */
export interface IngredientRequest {
  ingredients: string[]
}

/** Response data của POST /api/v1/recipes/suggest */
export interface RecipeSuggestionResponse {
  suggestedRecipes: Recipe[]
}
