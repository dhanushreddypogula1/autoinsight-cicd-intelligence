import { clsx } from 'clsx'

export { clsx as cx }

export const SEVERITY_CONFIG = {
  CRITICAL: { label: 'Critical', color: '#EF4444', bg: 'bg-red-500/10',    text: 'text-red-400',    border: 'border-red-500/30'    },
  HIGH:     { label: 'High',     color: '#F97316', bg: 'bg-orange-500/10', text: 'text-orange-400', border: 'border-orange-500/30' },
  MEDIUM:   { label: 'Medium',   color: '#EAB308', bg: 'bg-yellow-500/10', text: 'text-yellow-400', border: 'border-yellow-500/30' },
  LOW:      { label: 'Low',      color: '#22C55E', bg: 'bg-green-500/10',  text: 'text-green-400',  border: 'border-green-500/30'  },
}

export const CATEGORY_COLORS = {
  BUILD_FAILURE:      '#3B82F6',
  TEST_FAILURE:       '#8B5CF6',
  DEPENDENCY_FAILURE: '#F97316',
  DEPLOYMENT_FAILURE: '#EF4444',
  UNKNOWN_FAILURE:    '#6B7280',
}

export const CATEGORY_LABELS = {
  BUILD_FAILURE:      'Build Failure',
  TEST_FAILURE:       'Test Failure',
  DEPENDENCY_FAILURE: 'Dependency Failure',
  DEPLOYMENT_FAILURE: 'Deployment Failure',
  UNKNOWN_FAILURE:    'Unknown Failure',
}

export function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Intl.DateTimeFormat('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(dateStr))
}

export function formatBytes(bytes) {
  if (!bytes) return '0 B'
  const k    = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i    = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`
}

export function severityConfig(level) {
  return SEVERITY_CONFIG[level?.toUpperCase()] || SEVERITY_CONFIG.LOW
}
