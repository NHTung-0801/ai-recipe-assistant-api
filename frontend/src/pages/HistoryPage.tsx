import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { ChevronLeft, ChevronRight, Clock, Trash2, History as HistoryIcon } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { RecipeCard } from '@/components/RecipeCard'
import { RecipeDetailDialog } from '@/components/RecipeDetailDialog'
import { useHistoryList, useDeleteHistory } from '@/hooks/useHistory'
import type { Recipe } from '@/types/recipe'

const PAGE_SIZE = 5

export function HistoryPage() {
  const [page, setPage] = useState(0)
  const [selectedRecipe, setSelectedRecipe] = useState<Recipe | null>(null)
  const [expandedSessionId, setExpandedSessionId] = useState<string | null>(null)

  const { data, isLoading, isError } = useHistoryList({
    page,
    size: PAGE_SIZE,
    sort: 'searchTime,desc',
  })
  const deleteMutation = useDeleteHistory()

  const sessions = data?.content ?? []

  const handleDelete = (id: string, e: React.MouseEvent) => {
    e.stopPropagation()
    if (!confirm('Xoá phiên lịch sử này?')) return
    deleteMutation.mutate(id, {
      onSuccess: () => toast.success('Đã xoá'),
      onError: () => toast.error('Xoá thất bại'),
    })
  }

  return (
    <>
      <div className="container mx-auto max-w-5xl px-6 py-12 md:py-16 space-y-8">
        {/* Header */}
        <motion.section
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="space-y-3"
        >
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-primary/10 text-primary text-xs font-semibold uppercase tracking-widest">
            <HistoryIcon className="size-3.5" />
            Lịch sử
          </div>
          <h1 className="text-3xl md:text-4xl font-bold tracking-tight">
            Những món đã gợi ý
          </h1>
          <p className="text-muted-foreground">
            {data ? `Tổng cộng ${data.totalElements} phiên đã thực hiện` : 'Đang tải…'}
          </p>
        </motion.section>

        {/* Body */}
        {isLoading && (
          <div className="space-y-4">
            {[0, 1, 2].map((i) => (
              <div
                key={i}
                className="h-32 rounded-2xl bg-card animate-pulse border border-border"
              />
            ))}
          </div>
        )}

        {isError && (
          <div className="text-center py-12 text-destructive">
            Không thể tải lịch sử. Hãy kiểm tra backend đã chạy chưa.
          </div>
        )}

        {!isLoading && sessions.length === 0 && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="text-center py-20 space-y-3"
          >
            <div className="text-5xl animate-float inline-block">📖</div>
            <p className="text-muted-foreground">Chưa có phiên nào — quay lại trang chủ để bắt đầu</p>
          </motion.div>
        )}

        <AnimatePresence mode="popLayout">
          {sessions.map((session, idx) => {
            const isExpanded = expandedSessionId === session.id
            return (
              <motion.article
                key={session.id}
                layout
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ duration: 0.35, delay: idx * 0.05 }}
                className="rounded-2xl bg-card border border-border overflow-hidden shadow-sm hover:shadow-md transition-shadow"
              >
                {/* Session header */}
                <button
                  type="button"
                  onClick={() =>
                    setExpandedSessionId(isExpanded ? null : session.id)
                  }
                  className="w-full text-left p-5 hover:bg-accent/30 transition-colors"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 space-y-3 min-w-0">
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Clock className="size-3.5" />
                        {formatDate(session.searchTime)}
                        <span>·</span>
                        <span>{session.suggestedRecipes.length} món gợi ý</span>
                      </div>
                      <div className="flex flex-wrap gap-1.5">
                        {session.requestedIngredients.map((ing) => (
                          <Badge
                            key={ing}
                            variant="secondary"
                            className="bg-primary/10 text-primary border-0 font-medium"
                          >
                            {ing}
                          </Badge>
                        ))}
                      </div>
                    </div>
                    <div className="flex items-center gap-1 shrink-0">
                      <Button
                        size="icon"
                        variant="ghost"
                        onClick={(e) => handleDelete(session.id, e)}
                        disabled={deleteMutation.isPending}
                        aria-label="Xoá phiên này"
                        className="hover:bg-destructive/10 hover:text-destructive"
                      >
                        <Trash2 className="size-4" />
                      </Button>
                      <motion.div
                        animate={{ rotate: isExpanded ? 90 : 0 }}
                        transition={{ duration: 0.2 }}
                        className="text-muted-foreground"
                      >
                        <ChevronRight className="size-5" />
                      </motion.div>
                    </div>
                  </div>
                </button>

                {/* Expandable recipe grid */}
                <AnimatePresence initial={false}>
                  {isExpanded && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: 'auto', opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.3 }}
                      className="overflow-hidden"
                    >
                      <div className="p-5 pt-0 border-t border-border/50 bg-muted/20">
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 pt-5">
                          {session.suggestedRecipes.map((recipe, recipeIdx) => (
                            <RecipeCard
                              key={recipe.id}
                              recipe={recipe}
                              index={recipeIdx}
                              onClick={() => setSelectedRecipe(recipe)}
                            />
                          ))}
                        </div>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.article>
            )
          })}
        </AnimatePresence>

        {/* Pagination */}
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 pt-4">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={data.first}
            >
              <ChevronLeft className="size-4" />
              Trước
            </Button>
            <span className="text-sm text-muted-foreground px-3">
              Trang <span className="font-semibold text-foreground">{data.page + 1}</span> /{' '}
              {data.totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage((p) => p + 1)}
              disabled={data.last}
            >
              Tiếp
              <ChevronRight className="size-4" />
            </Button>
          </div>
        )}
      </div>

      <RecipeDetailDialog
        recipe={selectedRecipe}
        open={!!selectedRecipe}
        onOpenChange={(open) => !open && setSelectedRecipe(null)}
      />
    </>
  )
}

function formatDate(iso: string): string {
  // ISO LocalDateTime từ Java: "2026-06-30T22:33:50.165"
  const d = new Date(iso)
  return d.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
