import { useNavigate } from 'react-router-dom'
import {
  FileText, AlertTriangle, Zap, TrendingUp,
  ShieldAlert, ArrowRight, Activity,
} from 'lucide-react'
import { useFetch } from '../hooks/useFetch'
import { getDashboardStats, getAllIncidents } from '../services/api'
import {
  StatCard, Card, CardHeader, ErrorState, EmptyState,
} from '../components/ui'
import { SeverityDonut, CategoryBar } from '../components/charts/SeverityCharts'
import IncidentsTable from '../components/ui/IncidentsTable'
import { SeverityBadge, Skeleton } from '../components/ui'
import { formatDate } from '../utils/helpers'

function MostRecentIncident({ incident }) {
  const navigate = useNavigate()
  if (!incident) return null
  return (
    <div
      onClick={() => navigate(`/incidents/${incident.id}`)}
      className="mt-4 p-3 bg-surface-3 rounded-lg border border-border-dim hover:border-border-subtle cursor-pointer transition-all group"
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="text-xs font-medium text-slate-300 truncate">{incident.title}</p>
          <p className="text-[10px] text-slate-600 mt-0.5">{formatDate(incident.createdAt)}</p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <SeverityBadge level={incident.severityLevel} />
          <ArrowRight size={12} className="text-slate-600 group-hover:text-slate-400 transition-colors" />
        </div>
      </div>
    </div>
  )
}

export default function Dashboard() {
  const { data: stats, loading: sLoading, error: sError, refetch: refetchStats } = useFetch(getDashboardStats)
  const { data: incidents, loading: iLoading, error: iError } = useFetch(getAllIncidents)
  const navigate = useNavigate()

  const statCards = [
    { label: 'Logs Uploaded',   value: stats?.totalLogsUploaded,    icon: FileText,     color: '#3B82F6', sub: `${stats?.totalLogsProcessed ?? 0} processed` },
    { label: 'Total Incidents', value: stats?.totalIncidents,        icon: AlertTriangle,color: '#F97316', sub: `${stats?.criticalIncidents ?? 0} critical`   },
    { label: 'Errors Detected', value: stats?.totalErrorsDetected,   icon: Zap,          color: '#EF4444', sub: `${stats?.totalWarningsDetected ?? 0} warnings`  },
    { label: 'Exceptions',      value: stats?.totalExceptionsDetected,icon: Activity,    color: '#8B5CF6', sub: 'total exceptions'                              },
  ]

  if (sError) return <ErrorState message={sError} onRetry={refetchStats} />

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Stat Cards */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        {statCards.map((s) => (
          <StatCard key={s.label} {...s} loading={sLoading} />
        ))}
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Severity Donut */}
        <Card>
          <CardHeader
            title="Severity Distribution"
            subtitle="Incidents by severity level"
          />
          {sLoading
            ? <Skeleton className="h-[220px] w-full" />
            : <SeverityDonut data={stats?.incidentsBySeverity} />
          }
          <MostRecentIncident incident={stats?.mostRecentIncident} />
        </Card>

        {/* Category breakdown */}
        <Card>
          <CardHeader
            title="Failure Categories"
            subtitle="Incidents grouped by type"
          />
          {sLoading
            ? <div className="space-y-3">{[...Array(5)].map((_,i) => <Skeleton key={i} className="h-4 w-full" />)}</div>
            : <CategoryBar data={stats?.incidentsByCategory} />
          }
        </Card>

        {/* Severity quick stats */}
        <Card>
          <CardHeader title="Severity Counts" subtitle="Quick breakdown" />
          {sLoading
            ? <div className="space-y-3">{[...Array(4)].map((_,i) => <Skeleton key={i} className="h-10 w-full" />)}</div>
            : (
              <div className="space-y-2">
                {[
                  { label: 'Critical', value: stats?.criticalIncidents, color: 'bg-red-500'    },
                  { label: 'High',     value: stats?.highIncidents,     color: 'bg-orange-500' },
                  { label: 'Medium',   value: stats?.mediumIncidents,   color: 'bg-yellow-500' },
                  { label: 'Low',      value: stats?.lowIncidents,      color: 'bg-green-500'  },
                ].map(({ label, value, color }) => (
                  <div key={label} className="flex items-center justify-between p-3 bg-surface-3 rounded-lg border border-border-dim">
                    <div className="flex items-center gap-2">
                      <span className={`w-2 h-2 rounded-full ${color}`} />
                      <span className="text-sm text-slate-400">{label}</span>
                    </div>
                    <span className="font-display font-bold text-white tabular-nums">{value ?? 0}</span>
                  </div>
                ))}
              </div>
            )
          }
        </Card>
      </div>

      {/* Recent Incidents */}
      <Card>
        <CardHeader
          title="Recent Incidents"
          subtitle="Latest detected failures"
          action={
            <button onClick={() => navigate('/incidents')} className="btn-secondary text-xs">
              View all <ArrowRight size={12} />
            </button>
          }
        />
        <IncidentsTable incidents={incidents} loading={iLoading} compact />
      </Card>
    </div>
  )
}
