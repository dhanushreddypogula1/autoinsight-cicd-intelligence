# AutoInsight Frontend

Modern dark enterprise dashboard for the AutoInsight CI/CD intelligence platform.

## Tech Stack

- **React 18** + **Vite**
- **TailwindCSS** — dark enterprise design system
- **Axios** — API communication with response interceptors
- **Recharts** — severity and category charts
- **React Router v6** — client-side routing

## Project Structure

```
src/
├── components/
│   ├── layout/
│   │   ├── Layout.jsx        # App shell (sidebar + header)
│   │   ├── Sidebar.jsx       # Navigation sidebar
│   │   └── Header.jsx        # Top header bar
│   ├── ui/
│   │   ├── index.jsx         # Card, Badge, Skeleton, EmptyState, ErrorState, Toast
│   │   └── IncidentsTable.jsx # Reusable incidents table
│   └── charts/
│       └── SeverityCharts.jsx # SeverityDonut, CategoryBar
├── pages/
│   ├── Dashboard.jsx         # Main overview
│   ├── UploadLogs.jsx        # File upload with drag & drop
│   ├── Incidents.jsx         # List with search + severity filter
│   ├── IncidentDetail.jsx    # Full incident view
│   └── Analytics.jsx        # Charts & breakdowns
├── services/
│   └── api.js                # Axios instance + all API calls
├── hooks/
│   └── useFetch.js           # Generic data-fetching hook
└── utils/
    └── helpers.js            # formatDate, severityConfig, constants
```

## API Endpoints Connected

| Method | Endpoint                        | Page             |
|--------|---------------------------------|------------------|
| GET    | `/api/dashboard/stats`          | Dashboard, Analytics |
| GET    | `/api/incidents`                | Incidents, Dashboard |
| GET    | `/api/incidents/:id`            | Incident Detail  |
| GET    | `/api/incidents/severity/:level`| Incidents (filter) |
| POST   | `/api/logs/upload`              | Upload Logs      |

## Getting Started

```bash
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to `http://localhost:8080` (the Spring Boot backend).

For production builds:
```bash
npm run build
```

## Environment / Proxy

The `vite.config.js` proxies all `/api/*` calls to `http://localhost:8080`. If your backend runs on a different port, update `vite.config.js`:

```js
proxy: {
  '/api': {
    target: 'http://localhost:YOUR_PORT',
    changeOrigin: true,
  }
}
```
