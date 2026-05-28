import { useNavigate } from 'react-router-dom'
import { ChevronRight, AlertCircle } from 'lucide-react'
import { SeverityBadge, CategoryBadge, SkeletonTable, EmptyState } from '../ui'
import { formatDate, cx } from '../../utils/helpers'

export default function IncidentsTable({ incidents, loading, compact = false }) {
  const navigate = useNavigate()

  if (loading) return <SkeletonTable rows={compact ? 3 : 8} />

  if (!incidents?.length) {
    return (
      <EmptyState
        icon={AlertCircle}
        title="No incidents found"
        description="Upload a CI/CD log to start detecting incidents."
      />
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-border-dim">
            {['ID', 'Title', 'Severity', 'Category', 'Pipeline', 'Errors', 'Date', ''].map(h => (
              <th
                key={h}
                className="text-left text-[10px] font-mono text-slate-600 uppercase tracking-widest
                           pb-2 px-3 first:pl-0 last:pr-0 whitespace-nowrap"
              >
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {(compact ? incidents.slice(0, 5) : incidents).map((inc) => (
            <tr
              key={inc.id}
              onClick={() => navigate(`/incidents/${inc.id}`)}
              className="border-b border-border-dim/50 hover:bg-surface-3/50 cursor-pointer
                         transition-colors group"
            >
              <td className="py-3 px-3 pl-0">
                <span className="font-mono text-xs text-slate-600">#{inc.id}</span>
              </td>
              <td className="py-3 px-3 max-w-[240px]">
                <p className="text-slate-200 truncate text-xs font-medium">{inc.title}</p>
                {inc.logFileName && (
                  <p className="text-[10px] text-slate-600 mt-0.5 truncate font-mono">{inc.logFileName}</p>
                )}
              </td>
              <td className="py-3 px-3 whitespace-nowrap">
                <SeverityBadge level={inc.severityLevel} />
              </td>
              <td className="py-3 px-3 whitespace-nowrap">
                <CategoryBadge category={inc.failureCategory} display={inc.failureCategoryDisplay} />
              </td>
              <td className="py-3 px-3">
                <span className="text-xs text-slate-500 font-mono">{inc.pipelineName || '—'}</span>
              </td>
              <td className="py-3 px-3">
                <span className={cx(
                  'text-xs font-mono tabular-nums',
                  inc.errorCount > 5 ? 'text-red-400' : 'text-slate-400'
                )}>
                  {inc.errorCount ?? 0}
                </span>
              </td>
              <td className="py-3 px-3 whitespace-nowrap">
                <span className="text-xs text-slate-600">{formatDate(inc.createdAt)}</span>
              </td>
              <td className="py-3 px-3 pr-0">
                <ChevronRight
                  size={14}
                  className="text-slate-700 group-hover:text-slate-400 transition-colors"
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
