import { useState, useMemo } from 'react'
import { Search, Filter, RefreshCw } from 'lucide-react'
import { useFetch } from '../hooks/useFetch'
import { getAllIncidents, getIncidentsBySeverity } from '../services/api'
import { Card, CardHeader, ErrorState } from '../components/ui'
import IncidentsTable from '../components/ui/IncidentsTable'
import { cx } from '../utils/helpers'

const SEVERITIES = ['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW']

const SEVERITY_STYLES = {
  ALL:      'text-slate-400 border-border-dim hover:border-border-subtle',
  CRITICAL: 'text-red-400    border-red-500/30    bg-red-500/5',
  HIGH:     'text-orange-400 border-orange-500/30 bg-orange-500/5',
  MEDIUM:   'text-yellow-400 border-yellow-500/30 bg-yellow-500/5',
  LOW:      'text-green-400  border-green-500/30  bg-green-500/5',
}

export default function Incidents() {
  const [severity, setSeverity] = useState('ALL')
  const [search,   setSearch]   = useState('')

  const fetcher = useMemo(
    () => severity === 'ALL' ? getAllIncidents : () => getIncidentsBySeverity(severity),
    [severity]
  )

  const { data, loading, error, refetch } = useFetch(fetcher, [severity])

  const filtered = useMemo(() => {
    if (!data) return []
    const q = search.toLowerCase()
    if (!q) return data
    return data.filter(inc =>
      inc.title?.toLowerCase().includes(q) ||
      inc.logFileName?.toLowerCase().includes(q) ||
      inc.pipelineName?.toLowerCase().includes(q) ||
      inc.summary?.toLowerCase().includes(q)
    )
  }, [data, search])

  return (
    <div className="space-y-4 animate-fade-in">
      {/* Filters Bar */}
      <div className="flex flex-col sm:flex-row gap-3">
        {/* Severity Filter */}
        <div className="flex items-center gap-1.5 p-1 bg-surface-2 border border-border-dim rounded-lg">
          <Filter size={12} className="text-slate-600 ml-1.5" />
          {SEVERITIES.map(s => (
            <button
              key={s}
              onClick={() => setSeverity(s)}
              className={cx(
                'px-3 py-1 text-xs font-medium rounded-md border transition-all duration-150',
                severity === s
                  ? `${SEVERITY_STYLES[s]} font-semibold`
                  : 'text-slate-600 border-transparent hover:text-slate-400'
              )}
            >
              {s === 'ALL' ? 'All' : s.charAt(0) + s.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        {/* Search */}
        <div className="relative flex-1 sm:max-w-xs">
          <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-600" />
          <input
            className="input w-full pl-8"
            placeholder="Search incidents…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>

        <button onClick={refetch} className="btn-secondary ml-auto">
          <RefreshCw size={13} /> Refresh
        </button>
      </div>

      {/* Count */}
      <div className="flex items-center justify-between">
        <p className="text-xs text-slate-600 font-mono">
          {loading ? 'Loading…' : `${filtered.length} incident${filtered.length !== 1 ? 's' : ''} found`}
        </p>
        {search && (
          <button onClick={() => setSearch('')} className="text-xs text-accent-blue hover:underline">
            Clear search
          </button>
        )}
      </div>

      {/* Table */}
      <Card>
        {error
          ? <ErrorState message={error} onRetry={refetch} />
          : <IncidentsTable incidents={filtered} loading={loading} />
        }
      </Card>
    </div>
  )
}
