import { motion } from 'framer-motion'

interface MacroBarProps {
  protein: number
  carbs: number
  fat: number
  /** Khi true, hiển thị compact mode với chỉ thanh bar, không có label số gram */
  compact?: boolean
}

/**
 * Visualize tỉ lệ macros bằng stacked horizontal bar.
 * 3 màu: protein (terracotta), carbs (amber), fat (olive) - khớp với chart vars.
 */
export function MacroBar({ protein, carbs, fat, compact = false }: MacroBarProps) {
  const total = Math.max(protein + carbs + fat, 0.01) // tránh chia 0
  const segments = [
    { label: 'Đạm', value: protein, color: 'bg-chart-1', textColor: 'text-chart-1' },
    { label: 'Tinh bột', value: carbs, color: 'bg-chart-2', textColor: 'text-chart-2' },
    { label: 'Béo', value: fat, color: 'bg-chart-3', textColor: 'text-chart-3' },
  ]

  return (
    <div className="space-y-2">
      <div className="flex h-2 rounded-full overflow-hidden bg-muted">
        {segments.map((seg, i) => (
          <motion.div
            key={seg.label}
            initial={{ width: 0 }}
            animate={{ width: `${(seg.value / total) * 100}%` }}
            transition={{ duration: 0.6, delay: i * 0.1, ease: 'easeOut' }}
            className={seg.color}
          />
        ))}
      </div>
      {!compact && (
        <div className="flex justify-between text-xs">
          {segments.map((seg) => (
            <div key={seg.label} className="flex items-center gap-1.5">
              <span className={`size-2 rounded-full ${seg.color}`} />
              <span className="text-muted-foreground">{seg.label}</span>
              <span className="font-semibold tabular-nums">{seg.value}g</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
