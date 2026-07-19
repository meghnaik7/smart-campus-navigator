import axios from 'axios'

const api = axios.create({ baseURL: '/api/v1', timeout: 15000 })

// Attach JWT to every request
api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('accessToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

// Auto-refresh on 401
api.interceptors.response.use(
  r => r,
  async err => {
    if (err.response?.status === 401 && !err.config._retry) {
      err.config._retry = true
      const refresh = localStorage.getItem('refreshToken')
      if (refresh) {
        try {
          const { data } = await axios.post('/api/v1/auth/refresh', { refreshToken: refresh })
          localStorage.setItem('accessToken', data.accessToken)
          err.config.headers.Authorization = `Bearer ${data.accessToken}`
          return api(err.config)
        } catch { localStorage.clear(); window.location.href = '/login' }
      } else { window.location.href = '/login' }
    }
    return Promise.reject(err)
  }
)

export default api

export const authApi = {
  register: d => api.post('/auth/register', d),
  login:    d => api.post('/auth/login', d),
  refresh:  t => api.post('/auth/refresh', { refreshToken: t }),
}

export const buildingApi = {
  getAll:    ()    => api.get('/buildings'),
  getById:   id    => api.get(`/buildings/${id}`),
  getByType: type  => api.get(`/buildings/type/${type}`),
  search:    q     => api.get(`/buildings/search?q=${q}`),
  create:    d     => api.post('/buildings', d),
  update:    (id,d)=> api.put(`/buildings/${id}`, d),
  delete:    id    => api.delete(`/buildings/${id}`),
}

export const navApi = {
  route:        (from,to)   => api.get(`/navigate/route?from=${from}&to=${to}`),
  toFaculty:    (from,id)   => api.get(`/navigate/faculty/${id}?from=${from}`),
  toClassroom:  (from,id)   => api.get(`/navigate/classroom/${id}?from=${from}`),
  toEvent:      (from,id)   => api.get(`/navigate/event/${id}?from=${from}`),
  nearest:      (from,type) => api.get(`/navigate/nearest?from=${from}&type=${type}`),
}

export const nodeApi = {
  getAll:  () => api.get('/nodes'),
  create:  d  => api.post('/nodes', d),
  update:  (id,d) => api.put(`/nodes/${id}`, d),
  delete:  id => api.delete(`/nodes/${id}`),
  getEdges: () => api.get('/edges'),
  createEdge: d => api.post('/edges', d),
  deleteEdge: id => api.delete(`/edges/${id}`),
}

export const facultyApi = {
  getAll:  ()   => api.get('/faculty'),
  getById: id   => api.get(`/faculty/${id}`),
  search:  q    => api.get(`/faculty/search?q=${q}`),
  create:  d    => api.post('/faculty', d),
  update:  (id,d)=> api.put(`/faculty/${id}`, d),
  delete:  id   => api.delete(`/faculty/${id}`),
}

export const classroomApi = {
  getAll:          ()      => api.get('/classrooms'),
  getAvailable:    ()      => api.get('/classrooms?status=AVAILABLE'),
  search:          q       => api.get(`/classrooms/search?q=${q}`),
  create:          d       => api.post('/classrooms', d),
  update:          (id,d)  => api.put(`/classrooms/${id}`, d),
  setAvailability: (id,st) => api.patch(`/classrooms/${id}/availability?status=${st}`),
  delete:          id      => api.delete(`/classrooms/${id}`),
}

export const eventApi = {
  getAll:    () => api.get('/events'),
  getUpcoming:() => api.get('/events/upcoming'),
  create:    d  => api.post('/events', d),
  update:    (id,d) => api.put(`/events/${id}`, d),
  delete:    id => api.delete(`/events/${id}`),
}

export const searchApi = {
  suggest: (q, limit=10) => api.get(`/search/suggest?q=${encodeURIComponent(q)}&limit=${limit}`),
}

export const analyticsApi = {
  dashboard:       () => api.get('/analytics/dashboard'),
  topSearches:     () => api.get('/analytics/top-searches'),
  topDestinations: () => api.get('/analytics/top-destinations'),
}

export const userApi = {
  profile:        ()      => api.get('/users/profile'),
  update:         d       => api.put('/users/profile', d),
  changePassword: d       => api.put('/users/change-password', d),
  getAll:         (p=0)   => api.get(`/users?page=${p}`),
  search:         q       => api.get(`/users/search?q=${q}`),
  toggleStatus:   (id,v)  => api.patch(`/users/${id}/status`, { isActive: v }),
}
