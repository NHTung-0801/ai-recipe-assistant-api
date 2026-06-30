import { useMutation } from '@tanstack/react-query'
import { suggestRecipes } from '@/api/recipes'
import type { IngredientRequest } from '@/types/recipe'

/**
 * Hook gọi POST /recipes/suggest.
 * Dùng useMutation vì đây là action có side-effect (tạo Recipe, save UserHistory),
 * không phải data fetch thuần.
 *
 * Cách dùng trong component:
 *   const { mutate, isPending, data, error } = useSuggestRecipes()
 *   mutate({ ingredients: ['gà', 'tỏi'] })
 */
export function useSuggestRecipes() {
  return useMutation({
    mutationFn: (request: IngredientRequest) => suggestRecipes(request),
  })
}
