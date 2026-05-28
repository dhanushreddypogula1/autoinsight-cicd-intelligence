import { cx, severityConfig } from '../../utils/helpers'
import { AlertTriangle, Inbox, RefreshCw } from 'lucide-react'

// ─── Card ──────────────────────────────────────────────────────────────────

export function Card({ children, className, hover = false }) {
  return (
    <div className={cx(hover ? 'card-hover' : 'card', 'p-5', className)}>
      {children}
    </div>
  )
}

export function CardHeader({ title, subtitle, action }) {
  return (
    <div className="flex items-start justify-between mb-4">
      <div>
        <h3 className="font-display font-semibold text-white text-sm">{title}</h3>
        {subtitle && <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>}
      </div>
      {action}
    </div>
  )
}

// ─── Stat Card ─────────────────────────────────────────────────────────────

export function StatCard({ label, value, sub, icon: Icon, color = '#3B82F6', loading }) {
  if (loading) return <SkeletonCard />
  return (
    <div className="card-hover p-5 animate-fade-in">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-mono text-slate-500 uppercase tracking-wider">{label}</p>
          <p className="text-3xl font-display font-bold text-white mt-2 tabular-nums">{value ?? '—'}</p>
          {sub && <p className="text-xs text-slate-500 mt-1">{sub}</p>}
        </div>
        {Icon && (
          <div className="w-10 h-10 rounded-xl flex items-center justify-center"
               style={{ background: `${color}18`, color }}>
            <Icon size={20} strokeWidth={1.8} />
          </div>
        )}
      </div>
    </div>
  )
}

// ─── Severity Badge ────────────────────────────────────────────────────────

export function SeverityBadge({ level }) {
  const cfg = severityConfig(level)
  return (
    <span className={cx('badge border', cfg.bg, cfg.text, cfg.border)}>
      {cfg.label}
    </span>
  )
}

// ─── Category Badge ────────────────────────────────────────────────────────

export function CategoryBadge({ category, display }) {
  return (
    <span className="badge bg-surface-4 text-slate-400 border border-border-dim">
      {display || category}
    </span>
  )
}

// ─── Skeleton / Loading ────────────────────────────────────────────────────

export function Skeleton({ className }) {
  return <div className={cx('shimmer rounded', className)} />
}

export function SkeletonCard() {
  return (
    <div className="card p-5 space-y-3">
      <Skeleton className="h-3 w-20" />
      <Skeleton className="h-8 w-16" />
      <Skeleton className="h-3 w-32" />
    </div>
  )
}

export function SkeletonTable({ rows = 5 }) {
  return (
    <div className="space-y-2">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4 px-4 py-3">
          <Skeleton className="h-4 w-8"  />
          <Skeleton className="h-4 flex-1" />
          <Skeleton className="h-4 w-20" />
          <Skeleton className="h-4 w-16" />
        </div>
      ))}
    </div>
  )
}

// ─── Empty State ───────────────────────────────────────────────────────────

export function EmptyState({ icon: Icon = Inbox, title = 'No data', description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="w-14 h-14 rounded-2xl bg-surface-3 border border-border-dim flex items-center justify-center mb-4">
        <Icon size={24} className="text-slate-600" />
      </div>
      <p className="font-display font-semibold text-slate-400 text-sm">{title}</p>
      {description && <p className="text-xs text-slate-600 mt-1 max-w-xs">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}

// ─── Error State ───────────────────────────────────────────────────────────

export function ErrorState({ message, onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="w-14 h-14 rounded-2xl bg-red-500/10 border border-red-500/20 flex items-center justify-center mb-4">
        <AlertTriangle size={24} className="text-red-400" />
      </div>
      <p className="font-display font-semibold text-red-400 text-sm">Something went wrong</p>
      <p className="text-xs text-slate-600 mt-1 max-w-xs">{message}</p>
      {onRetry && (
        <button onClick={onRetry} className="btn-secondary mt-4 text-xs">
          <RefreshCw size={12} /> Try again
        </button>
      )}
    </div>
  )
}

// ─── Toast Notification ────────────────────────────────────────────────────

export function Toast({ type = 'success', message, onClose }) {
  const isError = type === 'error'
  return (
    <div className={cx(
      'fixed bottom-6 right-6 z-50 flex items-start gap-3 px-4 py-3 rounded-xl border shadow-xl animate-slide-up max-w-sm',
      isError
        ? 'bg-red-950/90 border-red-500/30 text-red-200'
        : 'bg-surface-3 border-border-subtle text-slate-200'
    )}>
      <span className="text-sm leading-snug">{message}</span>
      <button onClick={onClose} className="text-slate-500 hover:text-slate-300 transition-colors shrink-0">✕</button>
    </div>
  )
}
