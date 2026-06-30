import { motion } from 'framer-motion'
import { Clock, Flame } from 'lucide-react'
import type { Recipe } from '@/types/recipe'
import { Badge } from '@/components/ui/badge'
import { MacroBar } from './MacroBar'

interface RecipeCardProps {
  recipe: Recipe
  onClick?: () => void
  /** index dùng cho stagger animation */
  index?: number
}

/**
 * Card hiển thị 1 món ăn trong grid.
 * Hover: lift + glow nhẹ. Click toàn card để xem chi tiết.
 */
export function RecipeCard({ recipe, onClick, index = 0 }: RecipeCardProps) {
  return (
    <motion.button
      type="button"
      onClick={onClick}
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.08, ease: [0.22, 1, 0.36, 1] }}
      whileHover={{ y: -6 }}
      className="group relative text-left w-full rounded-2xl bg-card border border-border p-5 shadow-sm hover:shadow-xl hover:border-primary/40 transition-all duration-300 overflow-hidden"
    >
      {/* Decorative gradient circle ở góc - hiện rõ hơn khi hover */}
      <div className="absolute -top-12 -right-12 size-32 rounded-full bg-primary/10 blur-2xl group-hover:bg-primary/20 transition-colors" />

      <div className="relative space-y-4">
        <div className="space-y-2">
          <h3 className="font-semibold text-lg leading-tight group-hover:text-primary transition-colors line-clamp-2">
            {recipe.name}
          </h3>

          <div className="flex items-center gap-3 text-xs text-muted-foreground">
            <span className="inline-flex items-center gap-1">
              <Clock className="size-3.5" />
              {recipe.preparationTime} phút
            </span>
            <span className="inline-flex items-center gap-1">
              <Flame className="size-3.5 text-primary" />
              {recipe.estimatedCalories} kcal
            </span>
          </div>
        </div>

        <MacroBar
          protein={recipe.protein}
          carbs={recipe.carbs}
          fat={recipe.fat}
        />

        {recipe.tags && recipe.tags.length > 0 && (
          <div className="flex flex-wrap gap-1.5 pt-1">
            {recipe.tags.slice(0, 3).map((tag) => (
              <Badge
                key={tag}
                variant="secondary"
                className="text-[10px] font-medium uppercase tracking-wide bg-accent/50 text-accent-foreground border-0"
              >
                {tag}
              </Badge>
            ))}
            {recipe.tags.length > 3 && (
              <span className="text-[10px] text-muted-foreground self-center">
                +{recipe.tags.length - 3}
              </span>
            )}
          </div>
        )}
      </div>
    </motion.button>
  )
}

/**
 * Skeleton placeholder hiển thị khi AI đang xử lý (~2-5s).
 */
export function RecipeCardSkeleton({ index = 0 }: { index?: number }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ delay: index * 0.1 }}
      className="rounded-2xl bg-card border border-border p-5 space-y-4"
    >
      <div className="space-y-2">
        <div className="h-5 w-3/4 bg-muted animate-pulse rounded" />
        <div className="flex gap-3">
          <div className="h-3 w-16 bg-muted animate-pulse rounded" />
          <div className="h-3 w-20 bg-muted animate-pulse rounded" />
        </div>
      </div>
      <div className="h-2 w-full bg-muted animate-pulse rounded-full" />
      <div className="flex gap-1.5">
        <div className="h-5 w-14 bg-muted animate-pulse rounded-full" />
        <div className="h-5 w-16 bg-muted animate-pulse rounded-full" />
      </div>
    </motion.div>
  )
}
