import { Routes, Route } from 'react-router-dom'
import Layout        from './components/layout/Layout'
import Dashboard     from './pages/Dashboard'
import UploadLogs    from './pages/UploadLogs'
import Incidents     from './pages/Incidents'
import IncidentDetail from './pages/IncidentDetail'
import Analytics     from './pages/Analytics'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/"                 element={<Dashboard />}      />
        <Route path="/upload"           element={<UploadLogs />}     />
        <Route path="/incidents"        element={<Incidents />}      />
        <Route path="/incidents/:id"    element={<IncidentDetail />} />
        <Route path="/analytics"        element={<Analytics />}      />
      </Routes>
    </Layout>
  )
}
