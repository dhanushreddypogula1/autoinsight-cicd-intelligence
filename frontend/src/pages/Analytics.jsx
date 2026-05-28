import { useFetch } from '../hooks/useFetch'
import { getDashboardStats, getAllIncidents } from '../services/api'
import { Card, CardHeader, Skeleton, ErrorState } from '../components/ui'
import { SeverityDonut, CategoryBar } from '../components/charts/SeverityCharts'
import { formatDate, CATEGORY_COLORS, SEVERITY_CONFIG } from '../utils/helpers'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, LineChart, Line, PieChart, Pie,
} from 'recharts'

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-surface-3 border border-border-subtle rounded-lg px-3 py-2 shadow-xl">
      {label && <p className="text-[10px] text-slate-500 font-mono mb-1">{label}</p>}
      {payload.map((p, i) => (
        <p key={i} className="text-white font-display font-bold text-base tabular-nums" style={{ color: p.fill || p.color }}>
          {p.value}
        </p>
      ))}
    </div>
  )
}

function SeverityBarChart({ data }) {
  const COLORS = { Critical: '#EF4444', High: '#F97316', Medium: '#EAB308', Low: '#22C55E' }

  const chartData = Object.entries(data || {}).map(([name, value]) => ({
    name: SEVERITY_CONFIG[name]?.label || name,
    value: Number(value),
  }))

  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={chartData} barSize={40}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
        <XAxis dataKey="name" tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} />
        <YAxis tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} width={28} />
        <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.03)' }} />
        <Bar dataKey="value" radius={[5, 5, 0, 0]}>
          {chartData.map((entry, i) => (
            <Cell key={i} fill={COLORS[entry.name] || '#6B7280'} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}

function IncidentsTimeline({ incidents }) {
  // Group incidents by date
  const byDate = {}
  incidents?.forEach(inc => {
    const d = inc.createdAt ? new Date(inc.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : 'Unknown'
    byDate[d] = (byDate[d] || 0) + 1
  })

  const data = Object.entries(byDate)
    .slice(-14)
    .map(([date, count]) => ({ date, count }))

  if (!data.length) return (
    <div className="flex items-center justify-center h-48 text-slate-600 text-sm">No timeline data</div>
  )

  return (
    <ResponsiveContainer width="100%" height={200}>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
        <XAxis dataKey="date" tick={{ fill: '#64748B', fontSize: 10 }} axisLine={false} tickLine={false} />
        <YAxis tick={{ fill: '#64748B', fontSize: 10 }} axisLine={false} tickLine={false} width={24} allowDecimals={false} />
        <Tooltip content={<CustomTooltip />} cursor={{ stroke: 'rgba(59,130,246,0.3)', strokeWidth: 1 }} />
        <Line
          type="monotone"
          dataKey="count"
          stroke="#3B82F6"
          strokeWidth={2}
          dot={{ fill: '#3B82F6', r: 3, strokeWidth: 0 }}
          activeDot={{ r: 5, fill: '#3B82F6' }}
        />
      </LineChart>
    </ResponsiveContainer>
  )
}

function SummaryRow({ label, value, of, color }) {
  const pct = of ? Math.round((value / of) * 100) : 0
  return (
    <div className="flex items-center gap-3">
      <div className="flex-1 flex items-center justify-between">
        <span className="text-sm text-slate-400">{label}</span>
        <span className="font-mono text-white tabular-nums text-sm">{value}</span>
      </div>
      <div className="w-24 h-1.5 bg-surface-4 rounded-full overflow-hidden">
        <div className="h-full rounded-full transition-all duration-700" style={{ width: `${pct}%`, background: color }} />
      </div>
      <span className="w-8 text-[10px] font-mono text-slate-600 text-right">{pct}%</span>
    </div>
  )
}

export default function Analytics() {
  const { data: stats,     loading: sL, error: sE, refetch: rS } = useFetch(getDashboardStats)
  const { data: incidents, loading: iL, error: iE }              = useFetch(getAllIncidents)

  const total = stats?.totalIncidents || 0

  if (sE) return <ErrorState message={sE} onRetry={rS} />

  return (
    <div className="space-y-5 animate-fade-in">
      {/* Row 1: Severity bar + Donut */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader title="Incidents by Severity" subtitle="Count per severity level" />
          {sL
            ? <Skeleton className="h-[220px] w-full" />
            : <SeverityBarChart data={stats?.incidentsBySeverity} />
          }
        </Card>

        <Card>
          <CardHeader title="Severity Distribution" subtitle="Proportional breakdown" />
          {sL
            ? <Skeleton className="h-[220px] w-full" />
            : <SeverityDonut data={stats?.incidentsBySeverity} />
          }
        </Card>
      </div>

      {/* Row 2: Category breakdown + Timeline */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader title="Failure Categories" subtitle="Incidents by root cause type" />
          {sL
            ? <div className="space-y-3">{[...Array(5)].map((_,i) => <Skeleton key={i} className="h-5 w-full" />)}</div>
            : <CategoryBar data={stats?.incidentsByCategory} />
          }
        </Card>

        <Card>
          <CardHeader title="Incident Timeline" subtitle="Detections over time" />
          {iL
            ? <Skeleton className="h-[200px] w-full" />
            : <IncidentsTimeline incidents={incidents} />
          }
        </Card>
      </div>

      {/* Row 3: Detection summary */}
      <Card>
        <CardHeader title="Detection Summary" subtitle="Signal breakdown across all logs" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-3">
            <p className="text-[10px] font-mono text-slate-600 uppercase tracking-wider">Severity Breakdown</p>
            {sL ? <Skeleton className="h-24" /> : (
              <>
                <SummaryRow label="Critical" value={stats?.criticalIncidents ?? 0} of={total} color="#EF4444" />
                <SummaryRow label="High"     value={stats?.highIncidents ?? 0}     of={total} color="#F97316" />
                <SummaryRow label="Medium"   value={stats?.mediumIncidents ?? 0}   of={total} color="#EAB308" />
                <SummaryRow label="Low"      value={stats?.lowIncidents ?? 0}      of={total} color="#22C55E" />
              </>
            )}
          </div>
          <div className="space-y-3">
            <p className="text-[10px] font-mono text-slate-600 uppercase tracking-wider">Signal Counts</p>
            {sL ? <Skeleton className="h-24" /> : (
              <div className="grid grid-cols-3 gap-3">
                {[
                  { label: 'Errors',     value: stats?.totalErrorsDetected,     color: 'text-red-400'    },
                  { label: 'Warnings',   value: stats?.totalWarningsDetected,   color: 'text-yellow-400' },
                  { label: 'Exceptions', value: stats?.totalExceptionsDetected, color: 'text-orange-400' },
                ].map(({ label, value, color }) => (
                  <div key={label} className="p-3 bg-surface-3 rounded-xl border border-border-dim text-center">
                    <p className={`text-2xl font-display font-bold tabular-nums ${color}`}>{value ?? 0}</p>
                    <p className="text-[10px] text-slate-600 font-mono mt-1">{label}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </Card>
    </div>
  )
}
