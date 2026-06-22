
import { useState } from 'react'
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
} from 'lucide-react'

import { useFetch } from '../hooks/useFetch'
import {
  getIncidentById,
  generateAIAnalysis,
} from '../services/api'

import {
  Card,
  CardHeader,
  SeverityBadge,
  CategoryBadge,
  Skeleton,
  ErrorState,
} from '../components/ui'

import { formatDate, cx } from '../utils/helpers'

function MetaItem({ icon: Icon, label, value, mono = false }) {
  return (
    <div className="flex items-start gap-2.5 p-3 bg-surface-3 rounded-lg border border-border-dim">
      <Icon size={14} className="text-slate-600 shrink-0 mt-0.5" />
      <div className="min-w-0">
        <p className="text-[10px] text-slate-600 uppercase tracking-wider font-mono">
          {label}
        </p>
        <p
          className={cx(
            'text-sm text-slate-200 mt-0.5 truncate',
            mono ? 'font-mono' : 'font-medium'
          )}
        >
          {value || '—'}
        </p>
      </div>
    </div>
  )
}

function CountChip({ label, value, color }) {
  return (
    <div className={cx('flex flex-col items-center p-3 rounded-lg border', color)}>
      <span className="text-2xl font-display font-bold tabular-nums">
        {value ?? 0}
      </span>
      <span className="text-[10px] font-mono text-slate-500 mt-0.5">
        {label}
      </span>
    </div>
  )
}

function CodeBlock({ title, content, icon: Icon }) {
  if (!content) return null

  return (
    <div className="space-y-2">
      <div className="flex items-center gap-2">
        <Icon size={13} className="text-slate-600" />
        <p className="text-xs font-mono text-slate-500 uppercase tracking-wider">
          {title}
        </p>
      </div>

      <pre
        className="bg-surface-0 border border-border-dim rounded-xl p-4 text-xs
                   font-mono text-slate-300 overflow-x-auto whitespace-pre-wrap
                   leading-relaxed max-h-64 overflow-y-auto"
      >
        {content}
      </pre>
    </div>
  )
}

function SkeletonDetail() {
  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Skeleton className="h-6 w-2/3" />
        <Skeleton className="h-4 w-1/3" />
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {[...Array(4)].map((_, i) => (
          <Skeleton key={i} className="h-16" />
        ))}
      </div>

      <Skeleton className="h-32" />
      <Skeleton className="h-48" />
    </div>
  )
}

export default function IncidentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [aiAnalysis, setAiAnalysis] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)

  const {
    data: inc,
    loading,
    error,
    refetch,
  } = useFetch(() => getIncidentById(id), [id])

  const handleGenerateAI = async () => {
    try {
      setAiLoading(true)

      const result = await generateAIAnalysis({
        title: inc.title,
        summary: inc.summary,
        rawLogs: `${inc.rawErrorLines || ''}\n${inc.rawExceptionLines || ''}`,
      })

      setAiAnalysis(result)
    } catch (err) {
      alert(err.message)
    } finally {
      setAiLoading(false)
    }
  }

  if (loading)
    return (
      <div className="animate-fade-in">
        <SkeletonDetail />
      </div>
    )

  if (error)
    return (
      <ErrorState
        message={error}
        onRetry={refetch}
      />
    )

  if (!inc) return null

  return (
    <div className="space-y-5 animate-fade-in max-w-4xl">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-300 transition-colors"
      >
        <ArrowLeft size={13} />
        Back to incidents
      </button>

      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="flex items-center gap-2 mb-2">
            <span className="font-mono text-xs text-slate-600">
              #{inc.id}
            </span>

            <SeverityBadge level={inc.severityLevel} />

            <CategoryBadge
              category={inc.failureCategory}
              display={inc.failureCategoryDisplay}
            />
          </div>

          <h2 className="font-display font-bold text-white text-xl leading-snug">
            {inc.title}
          </h2>

          {inc.summary && (
            <p className="text-sm text-slate-400 mt-2 leading-relaxed">
              {inc.summary}
            </p>
          )}
        </div>
      </div>

      <div className="grid grid-cols-4 gap-3">
        <CountChip
          label="Errors"
          value={inc.errorCount}
          color="bg-red-500/5 border-red-500/20 text-red-400"
        />

        <CountChip
          label="Warnings"
          value={inc.warningCount}
          color="bg-yellow-500/5 border-yellow-500/20 text-yellow-400"
        />

        <CountChip
          label="Exceptions"
          value={inc.exceptionCount}
          color="bg-orange-500/5 border-orange-500/20 text-orange-400"
        />

        <CountChip
          label="Stack Traces"
          value={inc.stackTraceCount}
          color="bg-purple-500/5 border-purple-500/20 text-purple-400"
        />
      </div>

      <Card>
        <CardHeader title="Incident Metadata" />

        <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
          <MetaItem icon={Cpu} label="Pipeline" value={inc.pipelineName} />
          <MetaItem icon={GitBranch} label="Branch" value={inc.branchName} />
          <MetaItem icon={Layers} label="Stage" value={inc.pipelineStage} />
          <MetaItem icon={FileText} label="Log File" value={inc.logFileName} mono />
          <MetaItem icon={Hash} label="Log ID" value={`#${inc.logId}`} mono />
          <MetaItem icon={Calendar} label="Detected" value={formatDate(inc.createdAt)} />
        </div>
      </Card>

      <Card>
        <CardHeader title="Root Cause Analysis" />

        <div className="space-y-5">
          {inc.probableRootCause && (
            <div className="p-4 bg-surface-3 rounded-xl border border-border-dim">
              <div className="flex items-center gap-2 mb-2">
                <Bug size={13} className="text-orange-400" />
                <p className="text-xs font-mono text-slate-500 uppercase tracking-wider">
                  Probable Root Cause
                </p>
              </div>

              <p className="text-sm text-slate-300 leading-relaxed">
                {inc.probableRootCause}
              </p>
            </div>
          )}

          {inc.suggestedFix && (
            <div className="p-4 bg-surface-3 rounded-xl border border-accent-teal/20">
              <div className="flex items-center gap-2 mb-2">
                <Wrench size={13} className="text-accent-teal" />
                <p className="text-xs font-mono text-slate-500 uppercase tracking-wider">
                  Suggested Fix
                </p>
              </div>

              <p className="text-sm text-slate-300 leading-relaxed">
                {inc.suggestedFix}
              </p>
            </div>
          )}
        </div>
      </Card>

      <Card>
        <CardHeader
          title="🤖 AI Copilot Analysis"
          subtitle="Generate deeper AI-powered incident analysis"
        />

        <div className="space-y-4">
          <button
            onClick={handleGenerateAI}
            disabled={aiLoading}
            className="btn-primary"
          >
            {aiLoading ? (
              <>
                <Loader2 size={14} className="animate-spin" />
                Generating Analysis...
              </>
            ) : (
              <>
                <Sparkles size={14} />
                Generate AI Analysis
              </>
            )}
          </button>

          {aiAnalysis && (
            <div className="space-y-4">
              <div className="p-4 bg-surface-3 rounded-xl border border-border-dim">
                <h4 className="font-semibold text-orange-400 mb-2">
                  Root Cause
                </h4>

                <p className="text-slate-300 whitespace-pre-wrap">
                  {aiAnalysis.rootCause}
                </p>
              </div>

              <div className="p-4 bg-surface-3 rounded-xl border border-border-dim">
                <h4 className="font-semibold text-red-400 mb-2">
                  Business Impact
                </h4>

                <p className="text-slate-300 whitespace-pre-wrap">
                  {aiAnalysis.businessImpact}
                </p>
              </div>

              <div className="p-4 bg-surface-3 rounded-xl border border-border-dim">
                <h4 className="font-semibold text-green-400 mb-2">
                  Recommended Fix
                </h4>

                <p className="text-slate-300 whitespace-pre-wrap">
                  {aiAnalysis.recommendedFix}
                </p>
              </div>

              <div className="p-4 bg-surface-3 rounded-xl border border-accent-blue/20">
                <h4 className="font-semibold text-accent-blue mb-2">
                  Confidence Score
                </h4>

                <p className="text-2xl font-bold text-accent-blue">
                  {aiAnalysis.confidence}%
                </p>
              </div>
            </div>
          )}
        </div>
      </Card>

      {(inc.rawErrorLines || inc.rawExceptionLines) && (
        <Card>
          <CardHeader
            title="Raw Log Excerpts"
            subtitle="Extracted error and exception lines"
          />

          <div className="space-y-5">
            <CodeBlock
              title="Error Lines"
              content={inc.rawErrorLines}
              icon={AlertTriangle}
            />

            <CodeBlock
              title="Exception Lines"
              content={inc.rawExceptionLines}
              icon={Bug}
            />
          </div>
        </Card>
      )}
    </div>
  )
}
