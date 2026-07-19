import React, { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const NAV_LINKS = [
  { to:'/dashboard', icon:'fa-home',              label:'Dashboard' },
  { to:'/map',       icon:'fa-map',               label:'Map'       },
  { to:'/search',    icon:'fa-search',            label:'Search'    },
  { to:'/faculty',   icon:'fa-chalkboard-teacher',label:'Faculty'   },
  { to:'/classrooms',icon:'fa-door-open',         label:'Classrooms'},
  { to:'/events',    icon:'fa-calendar-alt',      label:'Events'    },
]

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth()
  const navigate  = useNavigate()
  const location  = useLocation()
  const [open, setOpen] = useState(false)

  const handleLogout = () => { logout(); navigate('/login') }
  const isActive = to => location.pathname === to

  const linkStyle = active => ({
    display:'flex', alignItems:'center', gap:'0.4rem',
    padding:'0.4rem 0.75rem', borderRadius:'0.6rem', textDecoration:'none',
    fontSize:'0.875rem', fontWeight:'500', transition:'all 0.15s',
    background: active ? '#2563eb' : 'transparent',
    color: active ? 'white' : '#94a3b8',
  })

  return (
    <nav style={{ position:'sticky', top:0, zIndex:50, background:'rgba(15,23,42,0.95)',
                  borderBottom:'1px solid rgba(255,255,255,0.08)', backdropFilter:'blur(12px)' }}>
      <div style={{ maxWidth:'1280px', margin:'0 auto', padding:'0 1rem',
                    display:'flex', alignItems:'center', justifyContent:'space-between', height:'3.75rem' }}>

        {/* Logo */}
        <Link to="/" style={{ display:'flex', alignItems:'center', gap:'0.6rem', textDecoration:'none' }}>
          <div style={{ width:'2.2rem', height:'2.2rem', background:'#2563eb', borderRadius:'0.6rem',
                        display:'flex', alignItems:'center', justifyContent:'center' }}>
            <i className="fas fa-compass" style={{ color:'white', fontSize:'1.1rem' }}></i>
          </div>
          <span style={{ fontWeight:'800', color:'white', fontSize:'1.1rem' }}>Smart Campus</span>
        </Link>

        {/* Desktop links */}
        {user && (
          <div style={{ display:'flex', alignItems:'center', gap:'0.25rem', flexWrap:'wrap' }}
               className="hidden-mobile">
            {NAV_LINKS.map(l => (
              <Link key={l.to} to={l.to} style={linkStyle(isActive(l.to))}
                onMouseOver={e => { if (!isActive(l.to)) e.currentTarget.style.background='rgba(255,255,255,0.06)' }}
                onMouseOut={e  => { if (!isActive(l.to)) e.currentTarget.style.background='transparent' }}>
                <i className={`fas ${l.icon}`} style={{ fontSize:'0.8rem' }}></i>{l.label}
              </Link>
            ))}
            {isAdmin() && (
              <Link to="/admin" style={linkStyle(isActive('/admin'))}
                onMouseOver={e => { if (!isActive('/admin')) e.currentTarget.style.background='rgba(245,158,11,0.1)' }}
                onMouseOut={e  => { if (!isActive('/admin')) e.currentTarget.style.background='transparent' }}>
                <i className="fas fa-shield-alt" style={{ fontSize:'0.8rem', color:'#fbbf24' }}></i>
                <span style={{ color: isActive('/admin') ? 'white' : '#fbbf24' }}>Admin</span>
              </Link>
            )}
          </div>
        )}

        {/* Right side */}
        <div style={{ display:'flex', alignItems:'center', gap:'0.5rem' }}>
          {user ? (
            <>
              {/* User badge */}
              <div style={{ display:'flex', alignItems:'center', gap:'0.5rem' }} className="hidden-mobile">
                <div style={{ width:'2rem', height:'2rem', borderRadius:'0.5rem', background:'#1d4ed8',
                              display:'flex', alignItems:'center', justifyContent:'center',
                              color:'white', fontWeight:'700', fontSize:'0.875rem' }}>
                  {user.firstName?.[0]?.toUpperCase()}
                </div>
                <div>
                  <div style={{ fontSize:'0.8rem', color:'white', fontWeight:'600', lineHeight:1 }}>
                    {user.firstName} {user.lastName}
                  </div>
                  <div style={{ fontSize:'0.65rem', color: isAdmin() ? '#fbbf24' : '#64748b' }}>
                    {isAdmin() ? '⚡ Admin' : 'Student'}
                  </div>
                </div>
              </div>
              <button onClick={handleLogout}
                style={{ padding:'0.4rem 0.875rem', borderRadius:'0.6rem', border:'1px solid rgba(255,255,255,0.12)',
                         background:'rgba(255,255,255,0.05)', color:'#94a3b8', cursor:'pointer',
                         fontSize:'0.8rem', fontWeight:'500' }}
                onMouseOver={e=>e.target.style.background='rgba(239,68,68,0.1)'}
                onMouseOut={e=>e.target.style.background='rgba(255,255,255,0.05)'}>
                <i className="fas fa-sign-out-alt" style={{ marginRight:'0.3rem' }}></i>Logout
              </button>
              {/* Mobile hamburger */}
              <button onClick={() => setOpen(!open)}
                style={{ padding:'0.4rem 0.6rem', borderRadius:'0.5rem', border:'1px solid rgba(255,255,255,0.1)',
                         background:'rgba(255,255,255,0.05)', color:'#94a3b8', cursor:'pointer' }}
                className="show-mobile">
                <i className={`fas ${open ? 'fa-times' : 'fa-bars'}`}></i>
              </button>
            </>
          ) : (
            <div style={{ display:'flex', gap:'0.5rem' }}>
              <Link to="/login" style={{ padding:'0.4rem 0.875rem', borderRadius:'0.6rem',
                border:'1px solid rgba(255,255,255,0.12)', background:'transparent',
                color:'#94a3b8', textDecoration:'none', fontSize:'0.875rem', fontWeight:'500' }}>
                Login
              </Link>
              <Link to="/register" style={{ padding:'0.4rem 0.875rem', borderRadius:'0.6rem',
                background:'#2563eb', color:'white', textDecoration:'none',
                fontSize:'0.875rem', fontWeight:'600' }}>
                Register
              </Link>
            </div>
          )}
        </div>
      </div>

      {/* Mobile menu */}
      {open && user && (
        <div style={{ background:'#0f172a', borderTop:'1px solid rgba(255,255,255,0.08)',
                      padding:'0.75rem 1rem', display:'flex', flexDirection:'column', gap:'0.25rem' }}>
          {NAV_LINKS.map(l => (
            <Link key={l.to} to={l.to} onClick={() => setOpen(false)}
              style={{ ...linkStyle(isActive(l.to)), padding:'0.6rem 0.75rem' }}>
              <i className={`fas ${l.icon}`} style={{ fontSize:'0.875rem' }}></i>{l.label}
            </Link>
          ))}
          {isAdmin() && (
            <Link to="/admin" onClick={() => setOpen(false)}
              style={{ ...linkStyle(isActive('/admin')), padding:'0.6rem 0.75rem' }}>
              <i className="fas fa-shield-alt" style={{ color:'#fbbf24' }}></i>
              <span style={{ color:'#fbbf24' }}>Admin</span>
            </Link>
          )}
        </div>
      )}

      <style>{`
        @media (min-width: 768px) { .hidden-mobile { display: flex !important; } .show-mobile { display: none !important; } }
        @media (max-width: 767px)  { .hidden-mobile { display: none !important; } .show-mobile { display: block !important; } }
      `}</style>
    </nav>
  )
}
