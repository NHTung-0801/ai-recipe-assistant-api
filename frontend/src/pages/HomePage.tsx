import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Sparkles, Loader2, UtensilsCrossed } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { IngredientInput } from '@/components/IngredientInput'
import { RecipeCard, RecipeCardSkeleton } from '@/components/RecipeCard'
import { RecipeDetailDialog } from '@/components/RecipeDetailDialog'
import { useSuggestRecipes } from '@/hooks/useSuggestRecipes'
import type { Recipe } from '@/types/recipe'

const QUICK_SUGGESTIONS = ['ức gà', 'bông cải xanh', 'cà chua', 'trứng', 'cơm', 'tỏi']

export function HomePage() {
  const [ingredients, setIngredients] = useState<string[]>([])
  const [selectedRecipe, setSelectedRecipe] = useState<Recipe | null>(null)
  const suggestMutation = useSuggestRecipes()

  const handleSuggest = () => {
    if (ingredients.length === 0) {
      toast.error('Hãy thêm ít nhất 1 nguyên liệu')
      return
    }
    suggestMutation.mutate(
      { ingredients },
      {
        onError: () => toast.error('AI đang bận, thử lại sau ít phút'),
      },
    )
  }

  const recipes = suggestMutation.data?.suggestedRecipes ?? []
  const hasResult = recipes.length > 0
  const isLoading = suggestMutation.isPending

  return (
    <>
      <div className="container mx-auto max-w-5xl px-6 py-12 md:py-16 space-y-10">
        {/* HERO */}
        <motion.section
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="text-center space-y-4"
        >
          <motion.div
            initial={{ scale: 0.85, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.1, type: 'spring', stiffness: 200 }}
            className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-primary/10 text-primary text-xs font-semibold uppercase tracking-widest"
          >
            <Sparkles className="size-3.5" />
            Powered by Gemini AI
          </motion.div>
          <h1 className="text-4xl md:text-5xl font-bold tracking-tight leading-tight">
            Hôm nay <span className="text-primary">ăn gì</span>?
          </h1>
          <p className="text-muted-foreground max-w-xl mx-auto">
            Nhập nguyên liệu bạn đang có, AI sẽ đề xuất những món ăn lành mạnh
            phù hợp với mục tiêu dinh dưỡng của bạn.
          </p>
        </motion.section>

        {/* INPUT CARD */}
        <motion.section
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.15 }}
          className="rounded-3xl bg-card border border-border p-6 md:p-8 shadow-sm space-y-5"
        >
          <div className="flex items-center gap-2">
            <UtensilsCrossed className="size-4 text-primary" />
            <h2 className="font-semibold">Nguyên liệu sẵn có</h2>
          </div>
          <IngredientInput
            value={ingredients}
            onChange={setIngredients}
            suggestions={QUICK_SUGGESTIONS}
          />
          <div className="flex items-center justify-between pt-2">
            <p className="text-xs text-muted-foreground">
              {ingredients.length === 0
                ? 'Tip: Bấm Enter sau mỗi nguyên liệu'
                : `${ingredients.length} nguyên liệu đã chọn`}
            </p>
            <Button
              onClick={handleSuggest}
              disabled={isLoading || ingredients.length === 0}
              size="lg"
              className="gap-2 min-w-[160px] shadow-md hover:shadow-lg transition-shadow"
            >
              {isLoading ? (
                <>
                  <Loader2 className="size-4 animate-spin" />
                  AI đang nghĩ…
                </>
              ) : (
                <>
                  <Sparkles className="size-4" />
                  Gợi ý món ăn
                </>
              )}
            </Button>
          </div>
        </motion.section>

        {/* RESULTS */}
        <section className="min-h-[200px]">
          <AnimatePresence mode="wait">
            {isLoading && (
              <motion.div
                key="loading"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"
              >
                {[0, 1, 2].map((i) => (
                  <RecipeCardSkeleton key={i} index={i} />
                ))}
              </motion.div>
            )}

            {!isLoading && hasResult && (
              <motion.div
                key="results"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="space-y-5"
              >
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-lg">
                    {recipes.length} gợi ý cho bạn
                  </h3>
                  <span className="text-xs text-muted-foreground">
                    Click để xem cách nấu
                  </span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                  {recipes.map((recipe, idx) => (
                    <RecipeCard
                      key={recipe.id}
                      recipe={recipe}
                      index={idx}
                      onClick={() => setSelectedRecipe(recipe)}
                    />
                  ))}
                </div>
              </motion.div>
            )}

            {!isLoading && !hasResult && (
              <motion.div
                key="empty"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="text-center py-12 space-y-3"
              >
                <div className="inline-block animate-float text-5xl">🍳</div>
                <p className="text-muted-foreground text-sm">
                  Thêm nguyên liệu vào ô bên trên rồi bấm{' '}
                  <span className="font-semibold text-foreground">Gợi ý món ăn</span> để bắt đầu
                </p>
              </motion.div>
            )}
          </AnimatePresence>
        </section>
      </div>

      <RecipeDetailDialog
        recipe={selectedRecipe}
        open={!!selectedRecipe}
        onOpenChange={(open) => !open && setSelectedRecipe(null)}
      />
    </>
  )
}
