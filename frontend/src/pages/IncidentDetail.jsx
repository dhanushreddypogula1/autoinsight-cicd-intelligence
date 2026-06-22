import { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  AlertTriangle,
  GitBranch,
  Cpu,
  Calendar,
  FileText,
  Hash,
  Layers,
  Bug,
  Wrench,
  Sparkles,
  Loader2,
  ShieldAlert,
  Clock,
  Target,
  CheckCircle2,
  ChevronRight,
  Zap,
  RefreshCw,
  ChevronDown,
  ChevronUp,
} from 'lucide-react'

import { useFetch } from '../hooks/useFetch'
import { getIncidentById, generateAIAnalysis } from '../services/api'
import {
  Card,
  CardHeader,
  SeverityBadge,
  CategoryBadge,
  Skeleton,
  ErrorState,
} from '../components/ui'
import { formatDate, cx } from '../utils/helpers'

// ─── Expandable text ──────────────────────────────────────────────────────────

function ExpandableText({ text, lines = 3 }) {
  const [expanded, setExpanded] = useState(false)
  if (!text) return <span className="text-slate-600 text-sm">—</span>

  const isLong = text.length > 180

  return (
    <div className="space-y-1.5">
      <p
        className={cx(
          'text-sm text-slate-300 leading-relaxed transition-all',
          !expanded && isLong ? `line-clamp-${lines}` : ''
        )}
      >
        {text}
      </p>
      {isLong && (
        <button
          onClick={() => setExpanded(v => !v)}
          className="flex items-center gap-1 text-[11px] font-mono text-accent-teal hover:text-accent-teal/80 transition-colors"
        >
          {expanded ? (
            <><ChevronUp size={11} /> Show less</>
          ) : (
            <><ChevronDown size={11} /> Show more</>
          )}
        </button>
      )}
    </div>
  )
}

// ─── Count chip ───────────────────────────────────────────────────────────────

function CountChip({ label, value, colorClass }) {
  return (
    <div className={cx('flex flex-col items-center justify-center p-3 rounded-xl border', colorClass)}>
      <span className="text-xl font-bold tabular-nums font-mono leading-none">{value ?? 0}</span>
      <span className="text-[10px] font-mono text-slate-500 mt-1 uppercase tracking-wide">{label}</span>
    </div>
  )
}

// ─── Meta item ────────────────────────────────────────────────────────────────

function MetaItem({ icon: Icon, label, value, mono = false }) {
  return (
    <div className="flex items-start gap-2.5 p-3 bg-surface-3 rounded-lg border border-border-dim">
      <Icon size={13} className="text-slate-600 shrink-0 mt-0.5" />
      <div className="min-w-0">
        <p className="text-[10px] text-slate-600 uppercase tracking-wider font-mono">{label}</p>
        <p className={cx('text-sm text-slate-200 mt-0.5 truncate', mono ? 'font-mono text-xs' : 'font-medium')}>
          {value || '—'}
        </p>
      </div>
    </div>
  )
}

// ─── AI metric card ───────────────────────────────────────────────────────────

function AIMetricCard({ icon: Icon, label, value, colorClass, iconColor }) {
  return (
    <div className={cx('p-3.5 rounded-xl border flex flex-col gap-1.5', colorClass)}>
      <div className="flex items-center gap-1.5">
        <Icon size={12} className={iconColor} />
        <p className="text-[10px] text-slate-500 uppercase font-mono tracking-wider">{label}</p>
      </div>
      <p className={cx('text-sm font-bold leading-tight break-words', iconColor)}>{value || '—'}</p>
    </div>
  )
}

// ─── AI analysis text block ───────────────────────────────────────────────────

function AITextBlock({ title, content, icon: Icon, iconColor, borderColor, defaultLines = 4 }) {
  if (!content) return null
  return (
    <div className={cx('p-4 bg-surface-3 rounded-xl border', borderColor)}>
      <div className="flex items-center gap-2 mb-2.5">
        <Icon size={13} className={iconColor} />
        <h4 className={cx('text-[11px] font-mono uppercase tracking-wider font-semibold', iconColor)}>
          {title}
        </h4>
      </div>
      <ExpandableText text={content} lines={defaultLines} />
    </div>
  )
}

// ─── Action plan ──────────────────────────────────────────────────────────────

function ActionPlanCard({ content }) {
  if (!content) return null

  const steps = content
    .split('\n')
    .map(l => l.trim())
    .filter(Boolean)
    .map(l => l.replace(/^(\d+[\.\)]|\-|\*)\s*/, '').trim())
    .filter(Boolean)

  return (
    <div className="p-4 bg-surface-3 rounded-xl border border-accent-teal/20">
      <div className="flex items-center gap-2 mb-3.5">
        <CheckCircle2 size={13} className="text-accent-teal" />
        <h4 className="text-[11px] font-mono uppercase tracking-wider font-semibold text-accent-teal">
          Action Plan
        </h4>
      </div>
      <ol className="space-y-2.5">
        {steps.map((step, i) => (
          <li key={i} className="flex items-start gap-3">
            <span className="flex-shrink-0 w-5 h-5 rounded-full bg-accent-teal/15 border border-accent-teal/30 flex items-center justify-center text-[10px] font-mono font-bold text-accent-teal mt-0.5">
              {i + 1}
            </span>
            <p className="text-sm text-slate-300 leading-relaxed">{step}</p>
          </li>
        ))}
      </ol>
    </div>
  )
}

// ─── Code block ───────────────────────────────────────────────────────────────

function CodeBlock({ title, content, icon: Icon }) {
  if (!content) return null
  return (
    <div className="space-y-2">
      <div className="flex items-center gap-2">
        <Icon size={13} className="text-slate-600" />
        <p className="text-xs font-mono text-slate-500 uppercase tracking-wider">{title}</p>
      </div>
      <pre className="bg-surface-0 border border-border-dim rounded-xl p-4 text-xs font-mono text-slate-300 overflow-x-auto whitespace-pre-wrap leading-relaxed max-h-56 overflow-y-auto">
        {content}
      </pre>
    </div>
  )
}

// ─── AI skeleton ──────────────────────────────────────────────────────────────

function AISkeleton() {
  return (
    <div className="space-y-4 animate-pulse">
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {[...Array(4)].map((_, i) => (
          <Skeleton key={i} className="h-16 rounded-xl" />
        ))}
      </div>
      <div className="border-t border-border-dim" />
      {[...Array(4)].map((_, i) => (
        <Skeleton key={i} className={cx('rounded-xl', i === 3 ? 'h-32' : 'h-20')} />
      ))}
    </div>
  )
}

// ─── Page skeleton ────────────────────────────────────────────────────────────

function PageSkeleton() {
  return (
    <div className="space-y-5">
      <Skeleton className="h-4 w-32" />
      <div className="space-y-2">
        <Skeleton className="h-7 w-2/3" />
        <Skeleton className="h-4 w-1/2" />
      </div>
      <div className="grid grid-cols-4 gap-3">
        {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-16 rounded-xl" />)}
      </div>
      <Skeleton className="h-40 rounded-xl" />
      <Skeleton className="h-64 rounded-xl" />
    </div>
  )
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function IncidentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [aiAnalysis, setAiAnalysis] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [aiError, setAiError] = useState(null)

  const { data: inc, loading, error, refetch } = useFetch(() => getIncidentById(id), [id])

  const handleGenerateAI = useCallback(async (incident) => {
    if (!incident) return
    try {
      setAiLoading(true)
      setAiError(null)
      const result = await generateAIAnalysis({
        title: incident.title,
        summary: incident.summary,
        rawLogs: `${incident.rawErrorLines || ''}\n${incident.rawExceptionLines || ''}`,
      })
      setAiAnalysis(result)
    } catch (err) {
      setAiError(err.message)
    } finally {
      setAiLoading(false)
    }
  }, [])

  // Auto-trigger once incident loads
  useEffect(() => {
    if (inc && !aiAnalysis && !aiLoading) {
      handleGenerateAI(inc)
    }
  }, [inc]) // eslint-disable-line react-hooks/exhaustive-deps

  if (loading) return <div className="animate-fade-in"><PageSkeleton /></div>
  if (error) return <ErrorState message={error} onRetry={refetch} />
  if (!inc) return null

  return (
    <div className="space-y-4 animate-fade-in max-w-4xl pb-10">

      {/* ── Back nav ── */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-300 transition-colors"
      >
        <ArrowLeft size={13} />
        Back to incidents
      </button>

      {/* ── Header ── */}
      <div className="space-y-2">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-mono text-xs text-slate-600">#{inc.id}</span>
          <SeverityBadge level={inc.severityLevel} />
          <CategoryBadge category={inc.failureCategory} display={inc.failureCategoryDisplay} />
        </div>
        <h2 className="font-display font-bold text-white text-lg sm:text-xl leading-snug">
          {inc.title}
        </h2>
        {inc.summary && (
          <p className="text-sm text-slate-400 leading-relaxed line-clamp-2">{inc.summary}</p>
        )}
      </div>

      {/* ── Detection counts ── */}
      <div className="grid grid-cols-4 gap-2 sm:gap-3">
        <CountChip label="Errors"       value={inc.errorCount}      colorClass="bg-red-500/5 border-red-500/20 text-red-400" />
        <CountChip label="Warnings"     value={inc.warningCount}    colorClass="bg-yellow-500/5 border-yellow-500/20 text-yellow-400" />
        <CountChip label="Exceptions"   value={inc.exceptionCount}  colorClass="bg-orange-500/5 border-orange-500/20 text-orange-400" />
        <CountChip label="Stack Traces" value={inc.stackTraceCount} colorClass="bg-purple-500/5 border-purple-500/20 text-purple-400" />
      </div>

      {/* ── Metadata ── */}
      <Card>
        <CardHeader title="Incident Metadata" />
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
          <MetaItem icon={Cpu}       label="Pipeline" value={inc.pipelineName} />
          <MetaItem icon={GitBranch} label="Branch"   value={inc.branchName} />
          <MetaItem icon={Layers}    label="Stage"    value={inc.pipelineStage} />
          <MetaItem icon={FileText}  label="Log File" value={inc.logFileName} mono />
          <MetaItem icon={Hash}      label="Log ID"   value={`#${inc.logId}`} mono />
          <MetaItem icon={Calendar}  label="Detected" value={formatDate(inc.createdAt)} />
        </div>
      </Card>

      {/* ── Compact RCA summary ── */}
      {(inc.probableRootCause || inc.suggestedFix) && (
        <Card>
          <CardHeader title="Detection Summary" subtitle="Parser-generated analysis" />
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {inc.probableRootCause && (
              <div className="p-3.5 bg-surface-3 rounded-xl border border-orange-500/15">
                <div className="flex items-center gap-1.5 mb-2">
                  <Bug size={12} className="text-orange-400" />
                  <p className="text-[10px] font-mono text-slate-500 uppercase tracking-wider">
                    Probable Root Cause
                  </p>
                </div>
                <ExpandableText text={inc.probableRootCause} lines={3} />
              </div>
            )}
            {inc.suggestedFix && (
              <div className="p-3.5 bg-surface-3 rounded-xl border border-accent-teal/15">
                <div className="flex items-center gap-1.5 mb-2">
                  <Wrench size={12} className="text-accent-teal" />
                  <p className="text-[10px] font-mono text-slate-500 uppercase tracking-wider">
                    Suggested Fix
                  </p>
                </div>
                <ExpandableText text={inc.suggestedFix} lines={3} />
              </div>
            )}
          </div>
        </Card>
      )}

      {/* ── AI Copilot ── */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <CardHeader
            title="🤖 AI Copilot Analysis"
            subtitle="Gemini-powered deep incident investigation"
          />
          {(aiAnalysis || aiError) && !aiLoading && (
            <button
              onClick={() => handleGenerateAI(inc)}
              className="flex items-center gap-1.5 text-[11px] text-slate-500 hover:text-slate-300 transition-colors border border-border-dim rounded-lg px-3 py-1.5 shrink-0"
            >
              <RefreshCw size={11} />
              <span className="hidden sm:inline">Regenerate</span>
            </button>
          )}
        </div>

        {/* Loading state */}
        {aiLoading && <AISkeleton />}

        {/* Error state */}
        {aiError && !aiLoading && (
          <div className="flex flex-col items-center gap-3 py-8 text-center">
            <AlertTriangle size={20} className="text-red-400" />
            <p className="text-sm text-slate-400">{aiError}</p>
            <button
              onClick={() => handleGenerateAI(inc)}
              className="btn-primary text-sm"
            >
              <RefreshCw size={13} />
              Retry
            </button>
          </div>
        )}

        {/* Analysis result */}
        {aiAnalysis && !aiLoading && (
          <div className="space-y-4">

            {/* Metric cards */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
              <AIMetricCard
                icon={ShieldAlert}
                label="Risk Level"
                value={aiAnalysis.riskLevel}
                colorClass="border-red-500/20 bg-red-500/5"
                iconColor="text-red-400"
              />
              <AIMetricCard
                icon={Target}
                label="Component"
                value={aiAnalysis.affectedComponent}
                colorClass="border-blue-500/20 bg-blue-500/5"
                iconColor="text-blue-400"
              />
              <AIMetricCard
                icon={Clock}
                label="Resolution"
                value={aiAnalysis.estimatedResolutionTime}
                colorClass="border-yellow-500/20 bg-yellow-500/5"
                iconColor="text-yellow-400"
              />
              <AIMetricCard
                icon={Zap}
                label="Confidence"
                value={aiAnalysis.confidence != null ? `${aiAnalysis.confidence}%` : null}
                colorClass="border-green-500/20 bg-green-500/5"
                iconColor="text-green-400"
              />
            </div>

            <div className="border-t border-border-dim" />

            {/* Text analysis blocks */}
            <AITextBlock
              title="Root Cause"
              content={aiAnalysis.rootCause}
              icon={Bug}
              iconColor="text-orange-400"
              borderColor="border-orange-500/20"
              defaultLines={4}
            />
            <AITextBlock
              title="Business Impact"
              content={aiAnalysis.businessImpact}
              icon={AlertTriangle}
              iconColor="text-red-400"
              borderColor="border-red-500/20"
              defaultLines={3}
            />
            <AITextBlock
              title="Recommended Fix"
              content={aiAnalysis.recommendedFix}
              icon={ChevronRight}
              iconColor="text-blue-400"
              borderColor="border-blue-500/20"
              defaultLines={4}
            />
            <ActionPlanCard content={aiAnalysis.actionPlan} />

          </div>
        )}
      </Card>

      {/* ── Raw log excerpts ── */}
      {(inc.rawErrorLines || inc.rawExceptionLines) && (
        <Card>
          <CardHeader
            title="Raw Log Excerpts"
            subtitle="Extracted error and exception lines"
          />
          <div className="space-y-5">
            <CodeBlock title="Error Lines"     content={inc.rawErrorLines}     icon={AlertTriangle} />
            <CodeBlock title="Exception Lines" content={inc.rawExceptionLines} icon={Bug} />
          </div>
        </Card>
      )}

    </div>
  )
}