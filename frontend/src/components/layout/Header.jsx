import { Bell, RefreshCw } from 'lucide-react'

const PAGE_TITLES = {
  '/':          { title: 'Dashboard',      subtitle: 'Live overview of your CI/CD health' },
  '/upload':    { title: 'Upload Logs',    subtitle: 'Analyze pipeline log files'          },
  '/incidents': { title: 'Incidents',      subtitle: 'Browse and filter detected failures' },
  '/analytics': { title: 'Analytics',      subtitle: 'Trends, categories, and severity breakdown' },
}

export default function Header({ pathname }) {
  const key    = Object.keys(PAGE_TITLES).find(k => k !== '/' && pathname.startsWith(k)) || '/'
  const meta   = PAGE_TITLES[key] || PAGE_TITLES['/']
  const now    = new Date().toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })

  return (
    <header className="h-16 flex items-center justify-between px-6 border-b border-border-dim bg-surface-1/80 backdrop-blur-sm shrink-0">
      <div>
        <h1 className="font-display font-semibold text-white text-lg leading-tight">{meta.title}</h1>
        <p className="text-xs text-slate-500 mt-0.5">{meta.subtitle}</p>
      </div>

      <div className="flex items-center gap-3">
        <span className="hidden sm:block text-xs text-slate-600 font-mono">{now}</span>

        <button className="w-8 h-8 flex items-center justify-center rounded-lg border border-border-dim text-slate-500 hover:text-slate-300 hover:bg-surface-3 transition-all">
          <Bell size={14} />
        </button>

        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-accent-blue to-accent-cyan flex items-center justify-center text-white text-xs font-display font-bold">
          A
        </div>
      </div>
    </header>
  )
}
