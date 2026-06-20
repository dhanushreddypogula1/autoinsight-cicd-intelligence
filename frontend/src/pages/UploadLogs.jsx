import { useState, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Upload, FileText, X, CheckCircle2, AlertCircle,
  Loader2, GitBranch, Cpu,
} from 'lucide-react'
import { uploadLog } from '../services/api'
import { Card, CardHeader, SeverityBadge, Toast } from '../components/ui'
import { formatBytes, formatDate, cx } from '../utils/helpers'

function FileDropzone({ file, onFile, onClear }) {
  const inputRef = useRef()
  const [dragging, setDragging] = useState(false)

  const handleDrop = useCallback((e) => {
    e.preventDefault()
    setDragging(false)
    const f = e.dataTransfer.files[0]
    if (f) onFile(f)
  }, [onFile])

  return (
    <div
      onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
      onDragLeave={() => setDragging(false)}
      onDrop={handleDrop}
      onClick={() => !file && inputRef.current?.click()}
      className={cx(
        'relative border-2 border-dashed rounded-xl p-10 text-center transition-all duration-200',
        file
          ? 'border-accent-blue/40 bg-accent-blue/5 cursor-default'
          : dragging
            ? 'border-accent-blue bg-accent-blue/10 cursor-copy'
            : 'border-border-subtle hover:border-accent-blue/40 hover:bg-surface-3/50 cursor-pointer'
      )}
    >
      <input
        ref={inputRef}
        type="file"
        accept=".txt,.log"
        className="hidden"
        onChange={(e) => e.target.files[0] && onFile(e.target.files[0])}
      />

      {file ? (
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-accent-blue/10 border border-accent-blue/20 flex items-center justify-center">
            <FileText size={22} className="text-accent-blue" />
          </div>
          <div>
            <p className="font-medium text-white text-sm">{file.name}</p>
            <p className="text-xs text-slate-500 mt-0.5">{formatBytes(file.size)}</p>
          </div>
          <button
            onClick={(e) => { e.stopPropagation(); onClear() }}
            className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-red-400 transition-colors mt-1"
          >
            <X size={12} /> Remove file
          </button>
        </div>
      ) : (
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-surface-3 border border-border-dim flex items-center justify-center">
            <Upload size={22} className="text-slate-500" />
          </div>
          <div>
            <p className="font-medium text-slate-300 text-sm">Drop your log file here</p>
            <p className="text-xs text-slate-600 mt-1">or click to browse — .txt files, max 10 MB</p>
          </div>
        </div>
      )}
    </div>
  )
}

function ProgressBar({ progress }) {
  return (
    <div className="space-y-1.5">
      <div className="flex justify-between text-xs">
        <span className="text-slate-400 font-mono">Uploading & analyzing…</span>
        <span className="text-accent-blue font-mono tabular-nums">{progress}%</span>
      </div>
      <div className="h-1.5 bg-surface-4 rounded-full overflow-hidden">
        <div
          className="h-full bg-gradient-to-r from-accent-blue to-accent-cyan rounded-full transition-all duration-300"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  )
}

function ResultCard({ result, onNavigate }) {
  return (
    <div className="p-4 bg-surface-3 border border-accent-blue/20 rounded-xl animate-slide-up">
      <div className="flex items-start gap-3 mb-4">
        <CheckCircle2 size={18} className="text-accent-emerald shrink-0 mt-0.5" />
        <div>
          <p className="text-sm font-medium text-white">Analysis complete</p>
          <p className="text-xs text-slate-500 mt-0.5">{result.message}</p>
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
        {[
          { label: 'Log ID',      value: `#${result.logId}`,           mono: true },
          { label: 'Status',      value: result.uploadStatus,          mono: true },
          { label: 'Incidents',   value: result.incidentsDetected,     highlight: result.incidentsDetected > 0 },
          { label: 'Pipeline',    value: result.pipelineName || '—' },
          { label: 'Branch',      value: result.branchName   || '—' },
          { label: 'Processed',   value: formatDate(result.processedAt) },
        ].map(({ label, value, mono, highlight }) => (
          <div key={label} className="bg-surface-4 rounded-lg p-2.5">
            <p className="text-[10px] text-slate-600 uppercase tracking-wider font-mono">{label}</p>
            <p className={cx(
              'text-sm mt-0.5 font-medium truncate',
              highlight ? 'text-accent-blue' : 'text-slate-200',
              mono ? 'font-mono' : ''
            )}>
              {value ?? '—'}
            </p>
          </div>
        ))}
      </div>

      {result.incidentsDetected > 0 && (
        <button
          onClick={onNavigate}
          className="btn-primary w-full mt-4 justify-center"
        >
          View {result.incidentsDetected} incident{result.incidentsDetected !== 1 ? 's' : ''} →
        </button>
      )}
    </div>
  )
}

export default function UploadLogs() {
  const navigate = useNavigate()
  const [file,         setFile]         = useState(null)
  const [pipelineName, setPipelineName] = useState('')
  const [branchName,   setBranchName]   = useState('')
  const [uploading,    setUploading]    = useState(false)
  const [progress,     setProgress]     = useState(0)
  const [result,       setResult]       = useState(null)
  const [toast,        setToast]        = useState(null)

  const showToast = (type, message) => {
    setToast({ type, message })
    setTimeout(() => setToast(null), 5000)
  }

  const handleSubmit = async () => {
    if (!file) return
    setUploading(true)
    setProgress(0)
    setResult(null)
    try {
      const res = await uploadLog({ file, pipelineName, branchName }, setProgress)
      setResult(res)
      showToast('success', 'Log analyzed successfully!')
    } catch (err) {
      showToast('error', err.message)
    } finally {
      setUploading(false)
    }
  }

  const handleClear = () => { setFile(null); setResult(null) }

  return (
    <div className="max-w-2xl mx-auto space-y-5 animate-fade-in">
      {toast && <Toast {...toast} onClose={() => setToast(null)} />}

      <Card>
        <CardHeader
          title="Upload CI/CD Log"
          subtitle="Supports .txt log files up to 10 MB"
        />

        <div className="mb-4 rounded-xl border border-yellow-500/30 bg-yellow-500/10 p-4">
          <div className="flex items-start gap-3">
            <AlertCircle size={18} className="text-yellow-400 mt-0.5 shrink-0" />
            <div>
              <p className="text-sm font-medium text-yellow-300">
                Demo Environment Notice
              </p>
              <p className="text-xs text-yellow-200 mt-1">
                AutoInsight AI is currently hosted on Render's free-tier infrastructure.
                If the backend has been idle, the first request may take approximately
                2–5 minutes while services wake up. Subsequent requests will respond normally.
              </p>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <FileDropzone file={file} onFile={setFile} onClear={handleClear} />

          {/* Metadata */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="flex items-center gap-1.5 text-xs text-slate-500 mb-1.5">
                <Cpu size={11} /> Pipeline Name
              </label>
              <input
                className="input w-full"
                placeholder="e.g. backend-ci"
                value={pipelineName}
                onChange={e => setPipelineName(e.target.value)}
              />
            </div>
            <div>
              <label className="flex items-center gap-1.5 text-xs text-slate-500 mb-1.5">
                <GitBranch size={11} /> Branch Name
              </label>
              <input
                className="input w-full"
                placeholder="e.g. main"
                value={branchName}
                onChange={e => setBranchName(e.target.value)}
              />
            </div>
          </div>

          {uploading && <ProgressBar progress={progress} />}

          <button
            onClick={handleSubmit}
            disabled={!file || uploading}
            className="btn-primary w-full justify-center py-2.5"
          >
            {uploading
              ? <><Loader2 size={15} className="animate-spin" /> Analyzing…</>
              : <><Upload size={15} /> Upload & Analyze</>
            }
          </button>
        </div>
      </Card>

      {result && (
        <ResultCard result={result} onNavigate={() => navigate('/incidents')} />
      )}

      {/* Tips */}
      <Card className="!p-4">
        <p className="text-xs font-mono text-slate-600 uppercase tracking-wider mb-3">Tips</p>
        <ul className="space-y-2">
          {[
            'Upload raw CI/CD log files from GitHub Actions, Jenkins, or GitLab CI.',
            'Include pipeline name and branch for better incident tracking.',
            'Logs are parsed for errors, exceptions, stack traces, and OOM events.',
          ].map((tip, i) => (
            <li key={i} className="flex items-start gap-2 text-xs text-slate-500">
              <span className="font-mono text-accent-blue mt-0.5">→</span>
              {tip}
            </li>
          ))}
        </ul>
      </Card>
    </div>
  )
}
