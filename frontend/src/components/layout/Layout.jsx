import { useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header  from './Header'

export default function Layout({ children }) {
  const { pathname } = useLocation()

  return (
    <div className="flex h-screen overflow-hidden bg-surface-0 bg-grid-dark">
      <Sidebar />
      <div className="flex flex-col flex-1 ml-60 min-w-0">
        <Header pathname={pathname} />
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  )
}
