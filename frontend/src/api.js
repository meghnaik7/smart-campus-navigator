// All HTTP calls to the Spring Boot backend live here.
// The Vite proxy forwards /api requests to http://localhost:8080

const BASE = '/api/v1'

// Read token from localStorage
function token() { return localStorage.getItem('token') }

// Authenticated JSON headers
function headers() {
  return {
    'Content-Type': 'application/json',
    ...(token() ? { Authorization: `Bearer ${token()}` } : {})
  }
}

// Generic fetch wrapper — throws the backend error message on failure
async function req(method, path, body) {
  const res = await fetch(BASE + path, {
    method,
    headers: headers(),
    body: body ? JSON.stringify(body) : undefined
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    // Use the backend's message, or validation errors if present
    const msg = data.validationErrors
      ? Object.values(data.validationErrors).join(', ')
      : data.message || `Error ${res.status}`
    throw new Error(msg)
  }
  return data
}

// Auth
export const login    = (email, password)   => req('POST', '/auth/login',    { email, password })
export const register = (firstName, email, password) =>
  req('POST', '/auth/register', { firstName, email, password })

// Graph nodes and edges
export const getNodes = ()          => req('GET',    '/nodes')
export const getEdges = ()          => req('GET',    '/edges')
export const addNode  = (body)      => req('POST',   '/nodes',       body)
export const delNode  = (id)        => req('DELETE', `/nodes/${id}`)
export const addEdge  = (body)      => req('POST',   '/edges',       body)
export const delEdge  = (id)        => req('DELETE', `/edges/${id}`)

// Navigation
export const getRoute = (from, to)  => req('GET', `/navigate/route?from=${from}&to=${to}`)
export const reloadGraph = ()       => req('POST', '/graph/reload')

// Search autocomplete
export const suggest  = (q)         => req('GET', `/search/suggest?q=${encodeURIComponent(q)}`)
