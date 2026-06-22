import {
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import { SEVERITY_CONFIG, CATEGORY_COLORS, CATEGORY_LABELS } from '../../utils/helpers'

const SEVERITY_COLORS = {
  CRITICAL: '#EF4444',
  HIGH:     '#F97316',
  MEDIUM:   '#EAB308',
  LOW:      '#22C55E',
}

const CustomTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null
  const { name, value } = payload[0]
  return (
    <div className="bg-surface-3 border border-border-subtle rounded-lg px-3 py-2 shadow-xl">
      <p className="text-xs text-slate-400 font-mono">{name}</p>
      <p className="text-white font-display font-bold text-lg tabular-nums">{value}</p>
    </div>
  )
}

export function SeverityDonut({ data }) {
  const chartData = Object.entries(data || {}).map(([name, value]) => {
    const severity = String(name).toUpperCase()
    return {
      name: SEVERITY_CONFIG[severity]?.label || name,
      value: Number(value),
      color: SEVERITY_COLORS[severity] || '#6B7280',
    }
  }).filter(d => d.value > 0)

  if (!chartData.length) {
    return (
      <div className="flex items-center justify-center h-48 text-slate-600 text-sm">
        No data available
      </div>
    )
  }

  return (
    <ResponsiveContainer width="100%" height={220}>
      <PieChart>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          innerRadius={60}
          outerRadius={90}
          paddingAngle={3}
          dataKey="value"
          strokeWidth={0}
        >
          {chartData.map((entry, i) => (
            <Cell key={i} fill={entry.color} opacity={0.9} />
          ))}
        </Pie>
        <Tooltip content={<CustomTooltip />} />
        <Legend
          formatter={(value) => (
            <span className="text-xs text-slate-400">{value}</span>
          )}
          iconType="circle"
          iconSize={8}
        />
      </PieChart>
    </ResponsiveContainer>
  )
}

export function CategoryBar({ data }) {
  const entries = Object.entries(data || {})
    .map(([key, value]) => ({
      label: CATEGORY_LABELS[key] || key,
      value: Number(value),
      color: CATEGORY_COLORS[key] || '#6B7280',
    }))
    .sort((a, b) => b.value - a.value)

  const max = Math.max(...entries.map(e => e.value), 1)

  return (
    <div className="space-y-3">
      {entries.map((entry, i) => (
        <div key={i}>
          <div className="flex items-center justify-between mb-1">
            <span className="text-xs text-slate-400 truncate pr-2">{entry.label}</span>
            <span className="text-xs font-mono text-slate-300 tabular-nums shrink-0">{entry.value}</span>
          </div>
          <div className="h-1.5 bg-surface-4 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full transition-all duration-700"
              style={{ width: `${(entry.value / max) * 100}%`, background: entry.color }}
            />
          </div>
        </div>
      ))}
      {entries.length === 0 && (
        <p className="text-xs text-slate-600 text-center py-4">No incidents recorded</p>
      )}
    </div>
  )
}

export function SeverityBarChart({ data }) {
  const { PieChart: _, ...rest } = {}
  const {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer: RC,
  } = require('recharts')

  const chartData = Object.entries(data || {}).map(([name, value]) => ({
    name: SEVERITY_CONFIG[name]?.label || name,
    value: Number(value),
    fill: SEVERITY_COLORS[name] || '#6B7280',
  }))

  return (
    <RC width="100%" height={200}>
      <BarChart data={chartData} barSize={32}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
        <XAxis dataKey="name" tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} />
        <YAxis tick={{ fill: '#64748B', fontSize: 11 }} axisLine={false} tickLine={false} width={28} />
        <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.03)' }} />
        <Bar dataKey="value" radius={[4, 4, 0, 0]}>
          {chartData.map((entry, i) => (
            <Cell key={i} fill={entry.fill} />
          ))}
        </Bar>
      </BarChart>
    </RC>
  )
}
