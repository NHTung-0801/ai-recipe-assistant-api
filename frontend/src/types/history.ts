import type { Recipe } from './recipe'

/**
 * 1 phiên lịch sử search.
 * Match UserHistoryResponse.java - chứa snapshot Recipe đã suggest.
 */
export interface UserHistory {
  id: string
  requestedIngredients: string[]
  suggestedRecipes: Recipe[]
  searchTime: string // ISO LocalDateTime từ Java
}
