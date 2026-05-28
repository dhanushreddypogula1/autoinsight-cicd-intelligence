import { NavLink, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, Upload, AlertTriangle, BarChart3,
  Zap, ChevronRight,
} from 'lucide-react'
import { cx } from '../../utils/helpers'

const NAV = [
  { to: '/',         icon: LayoutDashboard, label: 'Dashboard'        },
  { to: '/upload',   icon: Upload,          label: 'Upload Logs'      },
  { to: '/incidents',icon: AlertTriangle,   label: 'Incidents'        },
  { to: '/analytics',icon: BarChart3,       label: 'Analytics'        },
]

export default function Sidebar() {
  const { pathname } = useLocation()

  return (
    <aside className="fixed inset-y-0 left-0 w-60 flex flex-col z-30
                      bg-surface-1 border-r border-border-dim">
      {/* Logo */}
      <div className="h-16 flex items-center gap-3 px-5 border-b border-border-dim shrink-0">
        <div className="w-8 h-8 rounded-lg bg-accent-blue flex items-center justify-center shadow-lg shadow-blue-500/30">
          <Zap size={16} className="text-white" strokeWidth={2.5} />
        </div>
        <div>
          <p className="font-display font-700 text-[15px] text-white leading-none">AutoInsight</p>
          <p className="text-[10px] text-slate-500 mt-0.5 font-mono">CI/CD Intelligence</p>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-0.5">
        <p className="text-[10px] font-mono text-slate-600 uppercase tracking-widest px-3 pb-2">
          Navigation
        </p>
        {NAV.map(({ to, icon: Icon, label }) => {
          const active = to === '/' ? pathname === '/' : pathname.startsWith(to)
          return (
            <NavLink
              key={to}
              to={to}
              className={cx(
                'group flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
                active
                  ? 'bg-accent-blue/10 text-accent-blue border border-accent-blue/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-surface-3'
              )}
            >
              <Icon size={16} strokeWidth={active ? 2.5 : 2} />
              <span className="flex-1">{label}</span>
              {active && <ChevronRight size={12} className="opacity-60" />}
            </NavLink>
          )
        })}
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-border-dim">
        <div className="rounded-lg bg-surface-3 border border-border-dim px-3 py-2.5">
          <div className="flex items-center gap-2 mb-1">
            <span className="w-2 h-2 rounded-full bg-accent-emerald animate-pulse-slow" />
            <span className="text-xs text-slate-400 font-mono">API Connected</span>
          </div>
          <p className="text-[10px] text-slate-600 font-mono">https://autoinsight-backend-a2ai.onrender.com</p>
        </div>
      </div>
    </aside>
  )
}
