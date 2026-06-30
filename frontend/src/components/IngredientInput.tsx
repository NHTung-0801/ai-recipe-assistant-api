import { useState, type KeyboardEvent } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { Plus, X } from 'lucide-react'
import { cn } from '@/lib/utils'

interface IngredientInputProps {
  value: string[]
  onChange: (next: string[]) => void
  placeholder?: string
  suggestions?: string[]
}

/**
 * Chip-style ingredient input.
 * - Type + Enter (hoặc dấu phẩy) để thêm chip
 * - Backspace khi input rỗng để xoá chip cuối
 * - Click X hoặc chip suggestion để thêm/xoá
 */
export function IngredientInput({
  value,
  onChange,
  placeholder = 'Nhập nguyên liệu rồi bấm Enter…',
  suggestions = [],
}: IngredientInputProps) {
  const [draft, setDraft] = useState('')

  const addIngredient = (raw: string) => {
    const trimmed = raw.trim()
    if (!trimmed) return
    // Tránh duplicate (case-insensitive)
    if (value.some((v) => v.toLowerCase() === trimmed.toLowerCase())) return
    onChange([...value, trimmed])
    setDraft('')
  }

  const removeIngredient = (idx: number) => {
    onChange(value.filter((_, i) => i !== idx))
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault()
      addIngredient(draft)
    } else if (e.key === 'Backspace' && !draft && value.length > 0) {
      removeIngredient(value.length - 1)
    }
  }

  // Suggestions còn lại (loại bỏ những cái đã thêm rồi)
  const availableSuggestions = suggestions.filter(
    (s) => !value.some((v) => v.toLowerCase() === s.toLowerCase()),
  )

  return (
    <div className="space-y-4">
      <div
        className={cn(
          'relative flex flex-wrap items-center gap-2 min-h-[3.5rem] p-3 rounded-2xl',
          'bg-card/80 backdrop-blur border-2 border-border',
          'focus-within:border-primary focus-within:shadow-[0_0_0_4px_oklch(0.65_0.14_45/0.15)]',
          'transition-all duration-200',
        )}
      >
        <AnimatePresence mode="popLayout">
          {value.map((ingredient, idx) => (
            <motion.span
              key={ingredient}
              layout
              initial={{ opacity: 0, scale: 0.6, y: 8 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.6, y: -8 }}
              transition={{ type: 'spring', stiffness: 400, damping: 25 }}
              className="group inline-flex items-center gap-1.5 pl-3 pr-1.5 py-1 rounded-full bg-primary/10 text-primary text-sm font-medium border border-primary/20"
            >
              {ingredient}
              <button
                type="button"
                onClick={() => removeIngredient(idx)}
                className="size-5 rounded-full flex items-center justify-center hover:bg-primary/20 transition-colors"
                aria-label={`Xoá ${ingredient}`}
              >
                <X className="size-3" />
              </button>
            </motion.span>
          ))}
        </AnimatePresence>

        <input
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={value.length === 0 ? placeholder : ''}
          className="flex-1 min-w-[10rem] bg-transparent outline-none text-sm placeholder:text-muted-foreground"
        />
      </div>

      {availableSuggestions.length > 0 && (
        <div className="flex flex-wrap gap-2">
          <span className="text-xs text-muted-foreground self-center mr-1">
            Gợi ý nhanh:
          </span>
          {availableSuggestions.map((s) => (
            <motion.button
              key={s}
              type="button"
              onClick={() => addIngredient(s)}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium bg-secondary/60 hover:bg-secondary text-secondary-foreground border border-border transition-colors"
            >
              <Plus className="size-3" />
              {s}
            </motion.button>
          ))}
        </div>
      )}
    </div>
  )
}
