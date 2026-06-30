import { apiClient } from './client'
import type { ApiResponse } from '@/types/api'
import type { IngredientRequest, RecipeSuggestionResponse } from '@/types/recipe'

/**
 * POST /api/v1/recipes/suggest
 * Gửi danh sách nguyên liệu, nhận về 2-3 công thức từ AI.
 */
export async function suggestRecipes(
  request: IngredientRequest,
): Promise<RecipeSuggestionResponse> {
  const { data } = await apiClient.post<ApiResponse<RecipeSuggestionResponse>>(
    '/v1/recipes/suggest',
    request,
  )
  return data.data
}
