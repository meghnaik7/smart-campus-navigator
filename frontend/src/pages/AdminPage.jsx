import React, { useState, useEffect } from 'react'
import { analyticsApi, userApi, buildingApi, nodeApi } from '../services/api'
import api from '../services/api'
import toast from 'react-hot-toast'

// ── shared inline styles ──────────────────────────────────────────────────────
const card = {
  background: 'rgba(255,255,255,0.05)',
  border: '1px solid rgba(255,255,255,0.1)',
  borderRadius: '1rem',
  padding: '1.25rem',
}

const StatCard = ({ icon, label, value, bg }) => (
  <div style={{ ...card, display: 'flex', alignItems: 'center', gap: '1rem' }}>
    <div style={{
      width: '3rem', height: '3rem', borderRadius: '0.75rem',
      background: bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
    }}>
      <i className={`fas ${icon}`} style={{ color: 'white', fontSize: '1.2rem' }}></i>
    </div>
    <div>
      <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'white', lineHeight: 1 }}>
        {value ?? '-'}
      </div>
      <div style={{ fontSize: '0.8rem', color: '#94a3b8', marginTop: '0.15rem' }}>{label}</div>
    </div>
  </div>
)

const TabBtn = ({ label, icon, active, onClick }) => (
  <button onClick={onClick} style={{
    display: 'flex', alignItems: 'center', gap: '0.4rem',
    padding: '0.5rem 1rem', borderRadius: '0.6rem', border: 'none', cursor: 'pointer',
    fontSize: '0.875rem', fontWeight: 600, transition: 'all 0.15s',
    background: active ? '#2563eb' : 'transparent',
    color: active ? 'white' : '#94a3b8',
  }}>
    <i className={`fas ${icon}`}></i>{label}
  </button>
)

export default function AdminPage() {
  const [tab,       setTab]      = useState('dashboard')
  const [stats,     setStats]    = useState(null)
  const [topSearch, setTopSearch]= useState([])
  const [topDest,   setTopDest]  = useState([])
  const [users,     setUsers]    = useState([])
  const [buildings, setBuildings]= useState([])
  const [nodes,     setNodes]    = useState([])
  const [edges,     setEdges]    = useState([])

  // Load analytics once on mount
  useEffect(() => {
    analyticsApi.dashboard().then(r => setStats(r.data)).catch(() => {})
    analyticsApi.topSearches().then(r => setTopSearch(r.data || [])).catch(() => {})
    analyticsApi.topDestinations().then(r => setTopDest(r.data || [])).catch(() => {})
  }, [])

  // Load tab-specific data when tab changes
  useEffect(() => {
    if (tab === 'users')
      userApi.getAll().then(r => setUsers(r.data?.content || r.data || [])).catch(() => {})
    if (tab === 'buildings')
      buildingApi.getAll().then(r => setBuildings(r.data || [])).catch(() => {})
    if (tab === 'graph') {
      nodeApi.getAll().then(r  => setNodes(r.data || [])).catch(() => {})
      nodeApi.getEdges().then(r => setEdges(r.data || [])).catch(() => {})
    }
  }, [tab])

  const toggleUser = async (id, current) => {
    try {
      await userApi.toggleStatus(id, !current)
      toast.success('User status updated')
      userApi.getAll().then(r => setUsers(r.data?.content || r.data || [])).catch(() => {})
    } catch {
      toast.error('Failed to update user status')
    }
  }

  // Fixed: calls the actual POST /api/v1/graph/reload endpoint
  const reloadGraph = async () => {
    try {
      const r = await api.post('/graph/reload')
      toast.success(r.data?.message || 'Graph reloaded')
      nodeApi.getAll().then(r  => setNodes(r.data || [])).catch(() => {})
      nodeApi.getEdges().then(r => setEdges(r.data || [])).catch(() => {})
    } catch {
      toast.error('Graph reload failed')
    }
  }

  const tdStyle = { padding: '0.5rem 0.75rem', fontSize: '0.8rem' }

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1rem' }}>
      <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'white',
                   display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
        <i className="fas fa-shield-alt" style={{ color: '#fbbf24' }}></i> Admin Dashboard
      </h1>

      {/* Tab bar */}
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.25rem', padding: '0.4rem',
                    background: 'rgba(255,255,255,0.04)', borderRadius: '0.875rem',
                    border: '1px solid rgba(255,255,255,0.08)', width: 'fit-content', marginBottom: '1.5rem' }}>
        {[
          ['dashboard', 'fa-chart-bar', 'Analytics'],
          ['users',     'fa-users',     'Users'],
          ['buildings', 'fa-building',  'Buildings'],
          ['graph',     'fa-project-diagram', 'Graph'],
        ].map(([t, ic, lbl]) => (
          <TabBtn key={t} label={lbl} icon={ic} active={tab === t} onClick={() => setTab(t)} />
        ))}
      </div>

      {/* ── ANALYTICS TAB ─────────────────────────────────────────────── */}
      {tab === 'dashboard' && (
        <div>
          {stats && (
            <div style={{ display: 'grid',
                          gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                          gap: '1rem', marginBottom: '1.5rem' }}>
              {/*
                Fields match what AnalyticsService.dashboard() actually returns:
                  totalUsers, totalBuildings, totalFaculty, totalClassrooms,
                  totalSearches, totalRoutes
                The old code referenced stats.searchesToday etc. which don't exist
                in the backend response, so they always showed "-".
              */}
              <StatCard icon="fa-users"      label="Total Users"      value={stats.totalUsers}      bg="#2563eb" />
              <StatCard icon="fa-building"   label="Total Buildings"  value={stats.totalBuildings}  bg="#059669" />
              <StatCard icon="fa-chalkboard-teacher" label="Faculty"  value={stats.totalFaculty}    bg="#7c3aed" />
              <StatCard icon="fa-door-open"  label="Classrooms"       value={stats.totalClassrooms} bg="#d97706" />
              <StatCard icon="fa-search"     label="Total Searches"   value={stats.totalSearches}   bg="#db2777" />
              <StatCard icon="fa-route"      label="Total Routes"     value={stats.totalRoutes}     bg="#0891b2" />
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.25rem' }}>
            {/* Top searches */}
            <div style={card}>
              <div style={{ fontWeight: 700, color: 'white', marginBottom: '1rem',
                            display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <i className="fas fa-fire" style={{ color: '#f97316' }}></i> Top Searches
              </div>
              {topSearch.length === 0
                ? <p style={{ color: '#475569', fontSize: '0.875rem' }}>No search data yet</p>
                : topSearch.map((s, i) => (
                    <div key={i} style={{ display: 'flex', alignItems: 'center',
                                         justifyContent: 'space-between', padding: '0.5rem 0',
                                         borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                        <span style={{ width: '1.4rem', height: '1.4rem', borderRadius: '50%',
                                       background: 'rgba(255,255,255,0.06)', display: 'flex',
                                       alignItems: 'center', justifyContent: 'center',
                                       fontSize: '0.7rem', color: '#94a3b8' }}>{i + 1}</span>
                        <span style={{ color: 'white', fontSize: '0.875rem' }}>{s.query}</span>
                      </div>
                      <span style={{ color: '#60a5fa', fontSize: '0.75rem', fontWeight: 700 }}>
                        {s.count}x
                      </span>
                    </div>
                  ))}
            </div>

            {/* Top destinations */}
            <div style={card}>
              <div style={{ fontWeight: 700, color: 'white', marginBottom: '1rem',
                            display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <i className="fas fa-map-pin" style={{ color: '#60a5fa' }}></i> Top Destinations
              </div>
              {topDest.length === 0
                ? <p style={{ color: '#475569', fontSize: '0.875rem' }}>No route data yet</p>
                : topDest.map((d, i) => (
                    <div key={i} style={{ display: 'flex', alignItems: 'center',
                                         justifyContent: 'space-between', padding: '0.5rem 0',
                                         borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                        <span style={{ width: '1.4rem', height: '1.4rem', borderRadius: '50%',
                                       background: 'rgba(255,255,255,0.06)', display: 'flex',
                                       alignItems: 'center', justifyContent: 'center',
                                       fontSize: '0.7rem', color: '#94a3b8' }}>{i + 1}</span>
                        <span style={{ color: 'white', fontSize: '0.875rem' }}>{d.destination}</span>
                      </div>
                      <span style={{ color: '#4ade80', fontSize: '0.75rem', fontWeight: 700 }}>
                        {d.count}x
                      </span>
                    </div>
                  ))}
            </div>
          </div>
        </div>
      )}

      {/* ── USERS TAB ─────────────────────────────────────────────────── */}
      {tab === 'users' && (
        <div style={{ ...card, overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                {['ID', 'Name', 'Email', 'Role', 'Status', 'Action'].map(h => (
                  <th key={h} style={{ ...tdStyle, textAlign: 'left',
                                       color: '#64748b', fontWeight: 600 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                  <td style={{ ...tdStyle, color: '#64748b' }}>{u.id}</td>
                  <td style={tdStyle}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <div style={{
                        width: '1.75rem', height: '1.75rem', borderRadius: '0.4rem',
                        background: 'rgba(37,99,235,0.3)', display: 'flex',
                        alignItems: 'center', justifyContent: 'center',
                        fontSize: '0.75rem', fontWeight: 700, color: '#93c5fd',
                      }}>
                        {u.firstName?.[0]?.toUpperCase()}
                      </div>
                      <span style={{ color: 'white' }}>{u.firstName} {u.lastName}</span>
                    </div>
                  </td>
                  <td style={{ ...tdStyle, color: '#94a3b8' }}>{u.email}</td>
                  <td style={tdStyle}>
                    <span style={{
                      fontSize: '0.7rem', padding: '0.2rem 0.5rem', borderRadius: '999px',
                      border: '1px solid',
                      background: u.role === 'ROLE_ADMIN' ? 'rgba(245,158,11,0.1)' : 'rgba(59,130,246,0.1)',
                      borderColor: u.role === 'ROLE_ADMIN' ? 'rgba(245,158,11,0.3)' : 'rgba(59,130,246,0.3)',
                      color: u.role === 'ROLE_ADMIN' ? '#fbbf24' : '#60a5fa',
                    }}>
                      {u.role?.replace('ROLE_', '')}
                    </span>
                  </td>
                  <td style={tdStyle}>
                    <span style={{
                      fontSize: '0.7rem', padding: '0.2rem 0.5rem', borderRadius: '999px',
                      border: '1px solid',
                      background: u.isActive ? 'rgba(34,197,94,0.1)' : 'rgba(239,68,68,0.1)',
                      borderColor: u.isActive ? 'rgba(34,197,94,0.3)' : 'rgba(239,68,68,0.3)',
                      color: u.isActive ? '#4ade80' : '#f87171',
                    }}>
                      {u.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={tdStyle}>
                    <button onClick={() => toggleUser(u.id, u.isActive)} style={{
                      fontSize: '0.75rem', padding: '0.3rem 0.7rem', borderRadius: '0.4rem',
                      cursor: 'pointer', border: '1px solid', transition: 'all 0.15s',
                      background: u.isActive ? 'rgba(239,68,68,0.1)' : 'rgba(34,197,94,0.1)',
                      borderColor: u.isActive ? 'rgba(239,68,68,0.3)' : 'rgba(34,197,94,0.3)',
                      color: u.isActive ? '#f87171' : '#4ade80',
                    }}>
                      {u.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr><td colSpan={6} style={{ ...tdStyle, textAlign: 'center', color: '#475569', padding: '2rem' }}>
                  No users loaded
                </td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* ── BUILDINGS TAB ─────────────────────────────────────────────── */}
      {tab === 'buildings' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '1rem' }}>
          {buildings.map(b => (
            <div key={b.id} style={card}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
                <div style={{ width: '2.5rem', height: '2.5rem', borderRadius: '0.6rem',
                              background: 'rgba(37,99,235,0.2)', display: 'flex',
                              alignItems: 'center', justifyContent: 'center' }}>
                  <i className="fas fa-building" style={{ color: '#60a5fa' }}></i>
                </div>
                <div>
                  <div style={{ fontWeight: 700, color: 'white', fontSize: '0.9rem' }}>{b.name}</div>
                  <div style={{ fontSize: '0.7rem', color: '#64748b' }}>
                    {b.code ? `${b.code} | ` : ''}{b.type?.replace(/_/g, ' ')}
                  </div>
                </div>
              </div>
              <div style={{ fontSize: '0.75rem', color: '#475569' }}>
                {b.floors ? `${b.floors} floor${b.floors !== 1 ? 's' : ''}` : ''}
                {b.coordinateX != null ? ` | X:${b.coordinateX} Y:${b.coordinateY}` : ''}
              </div>
            </div>
          ))}
          {buildings.length === 0 && (
            <p style={{ color: '#475569', fontSize: '0.875rem' }}>No buildings loaded</p>
          )}
        </div>
      )}

      {/* ── GRAPH TAB ─────────────────────────────────────────────────── */}
      {tab === 'graph' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {/* Stats + reload button */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
            <div style={{ ...card, padding: '0.5rem 1rem', fontSize: '0.875rem', color: '#93c5fd' }}>
              <i className="fas fa-circle" style={{ marginRight: '0.4rem' }}></i>
              {nodes.length} Nodes
            </div>
            <div style={{ ...card, padding: '0.5rem 1rem', fontSize: '0.875rem', color: '#4ade80' }}>
              <i className="fas fa-project-diagram" style={{ marginRight: '0.4rem' }}></i>
              {edges.length} Edges
            </div>
            <button onClick={reloadGraph} style={{
              padding: '0.5rem 1.25rem', background: '#2563eb', border: 'none',
              borderRadius: '0.6rem', color: 'white', cursor: 'pointer',
              fontWeight: 700, fontSize: '0.875rem',
            }}>
              <i className="fas fa-sync" style={{ marginRight: '0.4rem' }}></i>Reload Graph
            </button>
          </div>

          {/* Nodes table */}
          <div style={{ ...card, overflowX: 'auto' }}>
            <div style={{ fontWeight: 700, color: 'white', marginBottom: '0.875rem' }}>Graph Nodes</div>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                  {['ID', 'Name', 'Type', 'X', 'Y', 'Floor', 'Building'].map(h => (
                    <th key={h} style={{ ...tdStyle, textAlign: 'left',
                                         color: '#64748b', fontWeight: 600 }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {nodes.map(n => (
                  <tr key={n.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                    <td style={{ ...tdStyle, color: '#475569' }}>{n.id}</td>
                    <td style={{ ...tdStyle, color: 'white'   }}>{n.name}</td>
                    <td style={{ ...tdStyle, color: '#60a5fa' }}>{n.nodeType?.replace(/_/g, ' ')}</td>
                    <td style={{ ...tdStyle, color: '#94a3b8' }}>{n.coordinateX}</td>
                    <td style={{ ...tdStyle, color: '#94a3b8' }}>{n.coordinateY}</td>
                    <td style={{ ...tdStyle, color: '#94a3b8' }}>{n.floor}</td>
                    <td style={{ ...tdStyle, color: '#94a3b8' }}>{n.buildingName || '-'}</td>
                  </tr>
                ))}
                {nodes.length === 0 && (
                  <tr><td colSpan={7} style={{ ...tdStyle, textAlign: 'center', color: '#475569', padding: '1.5rem' }}>
                    No nodes loaded
                  </td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
