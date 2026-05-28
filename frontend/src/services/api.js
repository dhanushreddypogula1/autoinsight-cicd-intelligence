import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// Response interceptor: unwrap ApiResponse wrapper
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg =
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      'An unexpected error occurred'
    return Promise.reject(new Error(msg))
  }
)

// ─── Dashboard ───────────────────────────────────────────────────────────────

export const getDashboardStats = async () => {
  const { data } = await api.get('/dashboard/stats')
  return data.data
}

// ─── Incidents ───────────────────────────────────────────────────────────────

export const getAllIncidents = async () => {
  const { data } = await api.get('/incidents')
  return data.data
}

export const getIncidentById = async (id) => {
  const { data } = await api.get(`/incidents/${id}`)
  return data.data
}

export const getIncidentsBySeverity = async (level) => {
  const { data } = await api.get(`/incidents/severity/${level}`)
  return data.data
}

// ─── Logs ────────────────────────────────────────────────────────────────────

export const uploadLog = async ({ file, pipelineName, branchName }, onProgress) => {
  const form = new FormData()
  form.append('file', file)
  if (pipelineName) form.append('pipelineName', pipelineName)
  if (branchName)   form.append('branchName',   branchName)

  const { data } = await api.post('/logs/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    },
  })
  return data.data
}

export default api
