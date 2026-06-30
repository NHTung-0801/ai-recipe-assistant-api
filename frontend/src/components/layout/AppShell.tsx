import { Link, NavLink, Outlet } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ChefHat, History as HistoryIcon } from 'lucide-react'
import { cn } from '@/lib/utils'

/**
 * AppShell - layout chung cho mọi page.
 * Header sticky + main content + footer tối giản.
 */
export function AppShell() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}

function Header() {
  return (
    <header className="sticky top-0 z-40 backdrop-blur-md bg-background/70 border-b border-border/50">
      <div className="container mx-auto max-w-6xl px-6 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 group">
          <motion.div
            whileHover={{ rotate: -8, scale: 1.05 }}
            transition={{ type: 'spring', stiffness: 300 }}
            className="size-9 rounded-xl bg-primary text-primary-foreground flex items-center justify-center shadow-sm"
          >
            <ChefHat className="size-5" strokeWidth={2.2} />
          </motion.div>
          <div className="flex flex-col leading-tight">
            <span className="font-semibold tracking-tight">Bếp Nhà</span>
            <span className="text-[10px] uppercase tracking-widest text-muted-foreground">
              AI Recipe Assistant
            </span>
          </div>
        </Link>

        <nav className="flex items-center gap-1">
          <HeaderLink to="/">Gợi ý</HeaderLink>
          <HeaderLink to="/history">
            <HistoryIcon className="size-4 mr-1.5" />
            Lịch sử
          </HeaderLink>
        </nav>
      </div>
    </header>
  )
}

function HeaderLink({ to, children }: { to: string; children: React.ReactNode }) {
  return (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) =>
        cn(
          'relative px-3 h-9 rounded-lg flex items-center text-sm font-medium transition-colors',
          'hover:bg-accent/60 hover:text-accent-foreground',
          isActive
            ? 'text-foreground bg-accent/40'
            : 'text-muted-foreground',
        )
      }
    >
      {({ isActive }) => (
        <>
          {children}
          {isActive && (
            <motion.span
              layoutId="active-nav"
              className="absolute inset-x-3 -bottom-[14px] h-[2px] bg-primary rounded-full"
              transition={{ type: 'spring', stiffness: 500, damping: 30 }}
            />
          )}
        </>
      )}
    </NavLink>
  )
}

function Footer() {
  return (
    <footer className="mt-auto border-t border-border/50 py-6">
      <div className="container mx-auto max-w-6xl px-6 text-center text-xs text-muted-foreground">
        Powered by Gemini · Built with Spring Boot 4 & React 19
      </div>
    </footer>
  )
}
