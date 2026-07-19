import React, { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { buildingApi, eventApi, analyticsApi } from '../services/api'

const card = { background:'rgba(255,255,255,0.05)', border:'1px solid rgba(255,255,255,0.1)',
               borderRadius:'1rem', padding:'1.25rem', boxShadow:'0 4px 20px rgba(0,0,0,0.3)' }

const StatCard = ({ icon, label, value, bg }) => (
  <div style={{ ...card, display:'flex', alignItems:'center', gap:'1rem' }}>
    <div style={{ width:'3rem', height:'3rem', borderRadius:'0.75rem', background:bg,
                  display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
      <i className={`fas ${icon}`} style={{ color:'white', fontSize:'1.2rem' }}></i>
    </div>
    <div>
      <div style={{ fontSize:'1.75rem', fontWeight:'800', color:'white', lineHeight:1 }}>{value ?? '—'}</div>
      <div style={{ fontSize:'0.8rem', color:'#94a3b8', marginTop:'0.15rem' }}>{label}</div>
    </div>
  </div>
)

const QuickBtn = ({ to, icon, label, desc, bg }) => {
  const navigate = useNavigate()
  return (
    <div onClick={() => navigate(to)} style={{ ...card, cursor:'pointer', transition:'all 0.2s' }}
      onMouseOver={e=>e.currentTarget.style.borderColor='rgba(59,130,246,0.4)'}
      onMouseOut={e=>e.currentTarget.style.borderColor='rgba(255,255,255,0.1)'}>
      <div style={{ width:'2.5rem', height:'2.5rem', borderRadius:'0.6rem', background:bg,
                    display:'flex', alignItems:'center', justifyContent:'center', marginBottom:'0.6rem' }}>
        <i className={`fas ${icon}`} style={{ color:'white', fontSize:'1.1rem' }}></i>
      </div>
      <div style={{ fontWeight:'700', color:'white', fontSize:'0.9rem' }}>{label}</div>
      <div style={{ color:'#64748b', fontSize:'0.75rem', marginTop:'0.2rem' }}>{desc}</div>
    </div>
  )
}

export default function DashboardPage() {
  const { user, isAdmin } = useAuth()
  const [buildings, setBuildings] = useState([])
  const [events,    setEvents]    = useState([])
  const [stats,     setStats]     = useState(null)

  useEffect(() => {
    buildingApi.getAll().then(r => setBuildings(Array.isArray(r.data)?r.data:[])).catch(()=>{})
    eventApi.getAll().then(r => setEvents((Array.isArray(r.data)?r.data:[]).slice(0,3))).catch(()=>{})
    if (isAdmin()) analyticsApi.dashboard().then(r => setStats(r.data)).catch(()=>{})
  }, [])

  return (
    <div style={{ maxWidth:'1200px', margin:'0 auto', padding:'2rem 1rem' }}>

      {/* Welcome banner */}
      <div style={{ ...card, background:'linear-gradient(135deg,rgba(37,99,235,0.3),rgba(124,58,237,0.2))',
                    borderColor:'rgba(59,130,246,0.2)', marginBottom:'1.5rem',
                    display:'flex', alignItems:'center', justifyContent:'space-between', flexWrap:'wrap', gap:'1rem' }}>
        <div>
          <h1 style={{ fontSize:'1.5rem', fontWeight:'800', color:'white', margin:'0 0 0.25rem' }}>
            Welcome back, {user?.firstName}! 👋
          </h1>
          <p style={{ color:'#94a3b8', margin:0, fontSize:'0.875rem' }}>
            {isAdmin() ? 'Admin Dashboard — Manage your campus data' : 'Navigate your campus with ease'}
          </p>
        </div>
        <Link to="/map" style={{ display:'inline-flex', alignItems:'center', gap:'0.4rem',
          padding:'0.6rem 1.25rem', background:'#2563eb', borderRadius:'0.75rem', color:'white',
          textDecoration:'none', fontWeight:'700', fontSize:'0.9rem' }}>
          <i className="fas fa-map-marked-alt"></i> Open Map
        </Link>
      </div>

      {/* Admin stats */}
      {isAdmin() && stats && (
        <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fit,minmax(200px,1fr))', gap:'1rem', marginBottom:'1.5rem' }}>
          <StatCard icon="fa-users"   label="Total Users"    value={stats.totalUsers}     bg="#2563eb"/>
          <StatCard icon="fa-building" label="Buildings"     value={stats.totalBuildings} bg="#059669"/>
          <StatCard icon="fa-search"  label="Total Searches" value={stats.totalSearches}  bg="#7c3aed"/>
          <StatCard icon="fa-route"   label="Total Routes"   value={stats.totalRoutes}    bg="#d97706"/>
        </div>
      )}

      {/* Quick links */}
      <div style={{ marginBottom:'1.5rem' }}>
        <h2 style={{ fontWeight:'700', color:'#94a3b8', marginBottom:'0.75rem', letterSpacing:'0.05em', textTransform:'uppercase', fontSize:'0.75rem' }}>
          QUICK NAVIGATE
        </h2>
        <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill,minmax(150px,1fr))', gap:'0.75rem' }}>
          <QuickBtn to="/map"        icon="fa-map"               label="Campus Map"  desc="Interactive map"  bg="#2563eb"/>
          <QuickBtn to="/search"     icon="fa-search"            label="Search"      desc="Find anything"    bg="#059669"/>
          <QuickBtn to="/faculty"    icon="fa-chalkboard-teacher"label="Faculty"     desc="Find professors"  bg="#7c3aed"/>
          <QuickBtn to="/classrooms" icon="fa-door-open"         label="Classrooms"  desc="Availability"     bg="#d97706"/>
          <QuickBtn to="/events"     icon="fa-calendar-alt"      label="Events"      desc="Upcoming events"  bg="#db2777"/>
          {isAdmin() && <QuickBtn to="/admin" icon="fa-shield-alt" label="Admin" desc="Manage campus" bg="#dc2626"/>}
        </div>
      </div>

      <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:'1.25rem' }}>
        {/* Buildings */}
        <div style={card}>
          <h2 style={{ fontWeight:'700', color:'white', marginBottom:'1rem', display:'flex', alignItems:'center', gap:'0.5rem', margin:'0 0 1rem' }}>
            <i className="fas fa-building" style={{ color:'#60a5fa' }}></i> Campus Buildings
          </h2>
          <div style={{ maxHeight:'280px', overflowY:'auto' }}>
            {buildings.slice(0,12).map(b => (
              <div key={b.id} style={{ display:'flex', alignItems:'center', justifyContent:'space-between',
                padding:'0.5rem 0.75rem', borderRadius:'0.6rem', marginBottom:'0.25rem',
                background:'rgba(255,255,255,0.03)', transition:'background 0.15s' }}
                onMouseOver={e=>e.currentTarget.style.background='rgba(255,255,255,0.07)'}
                onMouseOut={e=>e.currentTarget.style.background='rgba(255,255,255,0.03)'}>
                <div style={{ display:'flex', alignItems:'center', gap:'0.6rem' }}>
                  <div style={{ width:'1.75rem', height:'1.75rem', borderRadius:'0.4rem',
                                background:'rgba(59,130,246,0.15)', display:'flex', alignItems:'center', justifyContent:'center' }}>
                    <i className="fas fa-building" style={{ color:'#60a5fa', fontSize:'0.7rem' }}></i>
                  </div>
                  <div>
                    <div style={{ fontSize:'0.85rem', fontWeight:'600', color:'white' }}>{b.name}</div>
                    <div style={{ fontSize:'0.7rem', color:'#64748b' }}>{b.type?.replace(/_/g,' ')}</div>
                  </div>
                </div>
                <Link to="/map" style={{ color:'#60a5fa', fontSize:'0.75rem', textDecoration:'none' }}>
                  <i className="fas fa-compass"></i>
                </Link>
              </div>
            ))}
            {buildings.length === 0 && (
              <div style={{ textAlign:'center', color:'#475569', padding:'2rem', fontSize:'0.875rem' }}>
                <i className="fas fa-building" style={{ fontSize:'2rem', marginBottom:'0.5rem', display:'block' }}></i>
                No buildings loaded
              </div>
            )}
          </div>
        </div>

        {/* Events */}
        <div style={card}>
          <h2 style={{ fontWeight:'700', color:'white', marginBottom:'1rem', display:'flex', alignItems:'center', gap:'0.5rem', margin:'0 0 1rem' }}>
            <i className="fas fa-calendar-alt" style={{ color:'#f472b6' }}></i> Upcoming Events
          </h2>
          {events.length === 0 ? (
            <div style={{ textAlign:'center', color:'#475569', padding:'2rem', fontSize:'0.875rem' }}>
              <i className="fas fa-calendar-times" style={{ fontSize:'2rem', marginBottom:'0.5rem', display:'block' }}></i>
              No upcoming events
            </div>
          ) : (
            <div style={{ display:'flex', flexDirection:'column', gap:'0.75rem' }}>
              {events.map(ev => (
                <div key={ev.id} style={{ padding:'0.75rem', borderRadius:'0.6rem',
                  background:'rgba(255,255,255,0.03)', border:'1px solid rgba(255,255,255,0.06)' }}>
                  <div style={{ fontWeight:'600', color:'white', fontSize:'0.875rem' }}>{ev.title}</div>
                  <div style={{ fontSize:'0.75rem', color:'#64748b', marginTop:'0.3rem' }}>
                    <i className="fas fa-map-marker-alt" style={{ color:'#f472b6', marginRight:'0.25rem' }}></i>
                    {ev.venueName || ev.buildingName || 'TBD'}
                    <span style={{ margin:'0 0.4rem' }}>·</span>
                    <i className="fas fa-clock" style={{ color:'#60a5fa', marginRight:'0.25rem' }}></i>
                    {ev.startTime ? new Date(ev.startTime).toLocaleDateString() : 'TBD'}
                  </div>
                  <Link to="/events" style={{ fontSize:'0.75rem', color:'#60a5fa', textDecoration:'none', marginTop:'0.3rem', display:'inline-block' }}>
                    Navigate <i className="fas fa-arrow-right"></i>
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
