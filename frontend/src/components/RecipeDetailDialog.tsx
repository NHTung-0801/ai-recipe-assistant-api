import { motion } from 'framer-motion'
import { Clock, Flame, ChefHat, Sparkles } from 'lucide-react'
import type { Recipe } from '@/types/recipe'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { MacroBar } from './MacroBar'

interface RecipeDetailDialogProps {
  recipe: Recipe | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

/**
 * Dialog hiển thị chi tiết 1 recipe: ingredients, instructions từng bước,
 * macros lớn. Dùng chung cho HomePage và HistoryPage.
 */
export function RecipeDetailDialog({
  recipe,
  open,
  onOpenChange,
}: RecipeDetailDialogProps) {
  if (!recipe) return null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[88vh] p-0 overflow-hidden">
        {/* Hero header với gradient ấm */}
        <div className="relative bg-gradient-to-br from-primary/15 via-accent/40 to-primary/10 p-6 pb-5">
          <div className="absolute top-3 right-12 size-20 rounded-full bg-primary/20 blur-3xl" />
          <DialogHeader className="relative space-y-2.5">
            <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-primary font-semibold">
              <Sparkles className="size-3.5" />
              <span>Gợi ý từ AI</span>
            </div>
            <DialogTitle className="text-2xl font-bold tracking-tight leading-tight pr-8">
              {recipe.name}
            </DialogTitle>
            <DialogDescription className="flex items-center gap-4 text-sm">
              <span className="inline-flex items-center gap-1.5">
                <Clock className="size-4" />
                {recipe.preparationTime} phút
              </span>
              <span className="inline-flex items-center gap-1.5">
                <Flame className="size-4 text-primary" />
                {recipe.estimatedCalories} kcal
              </span>
            </DialogDescription>
          </DialogHeader>
        </div>

        <ScrollArea className="max-h-[calc(88vh-10rem)]">
          <div className="p-6 space-y-6">
            {/* Macros card */}
            <div className="rounded-2xl bg-muted/40 p-4 space-y-3 border border-border">
              <div className="flex items-center justify-between">
                <span className="text-sm font-semibold">Chỉ số dinh dưỡng</span>
                <span className="text-xs text-muted-foreground">
                  Trên 1 phần ăn
                </span>
              </div>
              <MacroBar
                protein={recipe.protein}
                carbs={recipe.carbs}
                fat={recipe.fat}
              />
            </div>

            {/* Tags */}
            {recipe.tags && recipe.tags.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {recipe.tags.map((tag) => (
                  <Badge
                    key={tag}
                    variant="secondary"
                    className="bg-accent/50 text-accent-foreground border-0 text-xs"
                  >
                    {tag}
                  </Badge>
                ))}
              </div>
            )}

            <Separator />

            {/* Ingredients */}
            <section>
              <h4 className="font-semibold mb-3 flex items-center gap-2">
                <span className="size-1.5 rounded-full bg-primary" />
                Nguyên liệu
              </h4>
              <ul className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {recipe.ingredients.map((ing, idx) => (
                  <motion.li
                    key={`${ing}-${idx}`}
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: idx * 0.03 }}
                    className="flex items-center gap-2 text-sm px-3 py-2 rounded-lg bg-muted/40"
                  >
                    <span className="size-1.5 rounded-full bg-primary/60 shrink-0" />
                    <span className="text-foreground">{ing}</span>
                  </motion.li>
                ))}
              </ul>
            </section>

            <Separator />

            {/* Instructions */}
            <section>
              <h4 className="font-semibold mb-3 flex items-center gap-2">
                <ChefHat className="size-4 text-primary" />
                Cách làm
              </h4>
              <ol className="space-y-3">
                {recipe.instructions.map((step, idx) => (
                  <motion.li
                    key={idx}
                    initial={{ opacity: 0, y: 6 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: idx * 0.05 }}
                    className="flex gap-3"
                  >
                    <div className="shrink-0 size-7 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-sm font-semibold">
                      {idx + 1}
                    </div>
                    <p className="text-sm leading-relaxed pt-0.5">{step}</p>
                  </motion.li>
                ))}
              </ol>
            </section>
          </div>
        </ScrollArea>
      </DialogContent>
    </Dialog>
  )
}
