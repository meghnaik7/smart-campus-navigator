import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate  = useNavigate()
  const [email,    setEmail]    = useState('')
  const [password, setPassword] = useState('')
  const [loading,  setLoading]  = useState(false)
  const [error,    setError]    = useState('')

  const fillAdmin   = () => { setEmail('admin@smartcampus.com');   setPassword('Admin@123');   setError('') }
  const fillStudent = () => { setEmail('student@university.edu');  setPassword('Student@123'); setError('') }

  const submit = async e => {
    e.preventDefault()
    setError('')
    if (!email.trim())    { setError('Email is required'); return }
    if (!password.trim()) { setError('Password is required'); return }
    setLoading(true)
    try {
      const user = await login(email.trim(), password)
      toast.success(`Welcome back, ${user.firstName}! 🎓`)
      navigate('/dashboard')
    } catch (err) {
      const msg = err?.response?.data?.message || 'Invalid email or password. Please try again.'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center',
                  background:'linear-gradient(135deg,#0f172a 0%,#1e1b4b 50%,#0f172a 100%)', padding:'1rem' }}>
      {/* Glow blobs */}
      <div style={{ position:'absolute', top:'-10rem', left:'-10rem', width:'30rem', height:'30rem',
                    background:'rgba(59,130,246,0.15)', borderRadius:'50%', filter:'blur(80px)', pointerEvents:'none' }}/>
      <div style={{ position:'absolute', bottom:'-10rem', right:'-10rem', width:'30rem', height:'30rem',
                    background:'rgba(139,92,246,0.15)', borderRadius:'50%', filter:'blur(80px)', pointerEvents:'none' }}/>

      <div style={{ position:'relative', width:'100%', maxWidth:'420px' }}>
        <div style={{ background:'rgba(255,255,255,0.05)', border:'1px solid rgba(255,255,255,0.1)',
                      borderRadius:'1.5rem', padding:'2.5rem', backdropFilter:'blur(20px)',
                      boxShadow:'0 25px 50px rgba(0,0,0,0.5)' }}>

          {/* Logo */}
          <div style={{ textAlign:'center', marginBottom:'2rem' }}>
            <div style={{ width:'4rem', height:'4rem', background:'#2563eb', borderRadius:'1rem',
                          display:'flex', alignItems:'center', justifyContent:'center',
                          margin:'0 auto 1rem', boxShadow:'0 10px 25px rgba(37,99,235,0.4)' }}>
              <i className="fas fa-compass" style={{ color:'white', fontSize:'1.75rem' }}></i>
            </div>
            <h1 style={{ fontSize:'1.5rem', fontWeight:'800', color:'white', margin:'0 0 0.25rem' }}>
              Smart Campus Navigator
            </h1>
            <p style={{ color:'#94a3b8', fontSize:'0.875rem' }}>Sign in to navigate your campus</p>
          </div>

          {/* Quick fill */}
          <div style={{ display:'flex', gap:'0.5rem', marginBottom:'1.25rem' }}>
            <button type="button" onClick={fillAdmin}
              style={{ flex:1, padding:'0.5rem', borderRadius:'0.75rem', border:'1px solid rgba(245,158,11,0.3)',
                       background:'rgba(245,158,11,0.1)', color:'#fbbf24', cursor:'pointer', fontSize:'0.75rem',
                       fontWeight:'600', transition:'all 0.2s' }}
              onMouseOver={e=>e.target.style.background='rgba(245,158,11,0.2)'}
              onMouseOut={e=>e.target.style.background='rgba(245,158,11,0.1)'}>
              <i className="fas fa-shield-alt" style={{ marginRight:'0.3rem' }}></i>Admin Login
            </button>
            <button type="button" onClick={fillStudent}
              style={{ flex:1, padding:'0.5rem', borderRadius:'0.75rem', border:'1px solid rgba(59,130,246,0.3)',
                       background:'rgba(59,130,246,0.1)', color:'#60a5fa', cursor:'pointer', fontSize:'0.75rem',
                       fontWeight:'600', transition:'all 0.2s' }}
              onMouseOver={e=>e.target.style.background='rgba(59,130,246,0.2)'}
              onMouseOut={e=>e.target.style.background='rgba(59,130,246,0.1)'}>
              <i className="fas fa-user-graduate" style={{ marginRight:'0.3rem' }}></i>Student Login
            </button>
          </div>

          {/* Error */}
          {error && (
            <div style={{ background:'rgba(239,68,68,0.1)', border:'1px solid rgba(239,68,68,0.3)',
                          borderRadius:'0.75rem', padding:'0.75rem 1rem', marginBottom:'1rem',
                          color:'#fca5a5', fontSize:'0.875rem', display:'flex', alignItems:'center', gap:'0.5rem' }}>
              <i className="fas fa-exclamation-circle"></i>{error}
            </div>
          )}

          <form onSubmit={submit}>
            {/* Email */}
            <div style={{ marginBottom:'1rem' }}>
              <label style={{ display:'block', color:'#94a3b8', fontSize:'0.875rem', marginBottom:'0.5rem', fontWeight:'500' }}>
                Email Address
              </label>
              <div style={{ position:'relative' }}>
                <i className="fas fa-envelope" style={{ position:'absolute', left:'0.875rem', top:'50%',
                  transform:'translateY(-50%)', color:'#64748b', fontSize:'0.875rem' }}></i>
                <input
                  type="email" value={email} onChange={e => setEmail(e.target.value)}
                  placeholder="admin@smartcampus.com" required
                  style={{ width:'100%', padding:'0.75rem 1rem 0.75rem 2.5rem', background:'rgba(255,255,255,0.06)',
                           border:'1px solid rgba(255,255,255,0.12)', borderRadius:'0.75rem', color:'white',
                           fontSize:'0.9rem', outline:'none', boxSizing:'border-box',
                           transition:'border-color 0.2s' }}
                  onFocus={e=>e.target.style.borderColor='#3b82f6'}
                  onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}
                />
              </div>
            </div>

            {/* Password */}
            <div style={{ marginBottom:'1.5rem' }}>
              <label style={{ display:'block', color:'#94a3b8', fontSize:'0.875rem', marginBottom:'0.5rem', fontWeight:'500' }}>
                Password
              </label>
              <div style={{ position:'relative' }}>
                <i className="fas fa-lock" style={{ position:'absolute', left:'0.875rem', top:'50%',
                  transform:'translateY(-50%)', color:'#64748b', fontSize:'0.875rem' }}></i>
                <input
                  type="password" value={password} onChange={e => setPassword(e.target.value)}
                  placeholder="••••••••" required
                  style={{ width:'100%', padding:'0.75rem 1rem 0.75rem 2.5rem', background:'rgba(255,255,255,0.06)',
                           border:'1px solid rgba(255,255,255,0.12)', borderRadius:'0.75rem', color:'white',
                           fontSize:'0.9rem', outline:'none', boxSizing:'border-box',
                           transition:'border-color 0.2s' }}
                  onFocus={e=>e.target.style.borderColor='#3b82f6'}
                  onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}
                />
              </div>
            </div>

            {/* Submit */}
            <button type="submit" disabled={loading}
              style={{ width:'100%', padding:'0.875rem', background: loading ? '#1d4ed8' : '#2563eb',
                       border:'none', borderRadius:'0.875rem', color:'white', fontSize:'1rem',
                       fontWeight:'700', cursor: loading ? 'not-allowed' : 'pointer',
                       transition:'all 0.2s', boxShadow:'0 4px 15px rgba(37,99,235,0.4)' }}
              onMouseOver={e=>{ if(!loading) e.target.style.background='#1d4ed8' }}
              onMouseOut={e=>{ if(!loading) e.target.style.background='#2563eb' }}>
              {loading
                ? <><i className="fas fa-spinner fa-spin" style={{ marginRight:'0.5rem' }}></i>Signing in...</>
                : <><i className="fas fa-sign-in-alt" style={{ marginRight:'0.5rem' }}></i>Sign In</>}
            </button>
          </form>

          <div style={{ textAlign:'center', marginTop:'1.5rem', borderTop:'1px solid rgba(255,255,255,0.08)', paddingTop:'1.5rem' }}>
            <span style={{ color:'#64748b', fontSize:'0.875rem' }}>Don't have an account? </span>
            <Link to="/register" style={{ color:'#60a5fa', fontWeight:'600', textDecoration:'none' }}>Register here</Link>
          </div>

          {/* Credentials hint */}
          <div style={{ marginTop:'1rem', background:'rgba(255,255,255,0.03)', borderRadius:'0.75rem',
                        padding:'0.75rem', fontSize:'0.75rem', color:'#475569', textAlign:'center' }}>
            <strong style={{ color:'#64748b' }}>Demo Accounts:</strong><br/>
            Admin: admin@smartcampus.com / Admin@123<br/>
            Student: student@university.edu / Student@123
          </div>
        </div>
      </div>
    </div>
  )
}
