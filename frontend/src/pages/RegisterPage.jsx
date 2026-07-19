import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

const S = { // inline styles shortcuts
  label: { display:'block', color:'#94a3b8', fontSize:'0.8rem', marginBottom:'0.4rem', fontWeight:'500' },
  input: { width:'100%', padding:'0.6rem 0.875rem', background:'rgba(255,255,255,0.06)',
           border:'1px solid rgba(255,255,255,0.12)', borderRadius:'0.6rem', color:'white',
           fontSize:'0.875rem', outline:'none', boxSizing:'border-box' },
  select: { width:'100%', padding:'0.6rem 0.875rem', background:'#1e293b',
            border:'1px solid rgba(255,255,255,0.12)', borderRadius:'0.6rem', color:'white',
            fontSize:'0.875rem', outline:'none', boxSizing:'border-box' },
}

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    firstName:'', lastName:'', email:'', password:'',
    studentId:'', department:'', yearOfStudy:''
  })
  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState('')

  const set = k => e => { setForm(f => ({...f, [k]: e.target.value})); setError('') }

  const submit = async e => {
    e.preventDefault()
    setError('')
    if (!form.firstName.trim()) { setError('First name is required'); return }
    if (!form.email.trim())     { setError('Email is required'); return }
    if (form.password.length < 8) { setError('Password must be at least 8 characters'); return }

    setLoading(true)
    try {
      const payload = { ...form }
      if (form.yearOfStudy) payload.yearOfStudy = Number(form.yearOfStudy)
      else delete payload.yearOfStudy
      const user = await register(payload)
      toast.success(`Welcome, ${user.firstName}! 🎉`)
      navigate('/dashboard')
    } catch (err) {
      const msg = err?.response?.data?.message || 'Registration failed. Please try again.'
      setError(msg)
      toast.error(msg)
    } finally { setLoading(false) }
  }

  const wrap = { marginBottom:'0.875rem' }
  const grid2 = { display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0.75rem' }

  return (
    <div style={{ minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center',
                  background:'linear-gradient(135deg,#0f172a 0%,#1e1b4b 50%,#0f172a 100%)',
                  padding:'2rem 1rem' }}>
      <div style={{ position:'absolute', top:'-8rem', right:'-8rem', width:'25rem', height:'25rem',
                    background:'rgba(59,130,246,0.12)', borderRadius:'50%', filter:'blur(80px)', pointerEvents:'none' }}/>

      <div style={{ position:'relative', width:'100%', maxWidth:'500px' }}>
        <div style={{ background:'rgba(255,255,255,0.05)', border:'1px solid rgba(255,255,255,0.1)',
                      borderRadius:'1.5rem', padding:'2rem', backdropFilter:'blur(20px)',
                      boxShadow:'0 25px 50px rgba(0,0,0,0.5)' }}>

          {/* Header */}
          <div style={{ textAlign:'center', marginBottom:'1.5rem' }}>
            <div style={{ width:'3.5rem', height:'3.5rem', background:'#2563eb', borderRadius:'0.875rem',
                          display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 0.75rem',
                          boxShadow:'0 8px 20px rgba(37,99,235,0.4)' }}>
              <i className="fas fa-user-plus" style={{ color:'white', fontSize:'1.5rem' }}></i>
            </div>
            <h1 style={{ fontSize:'1.4rem', fontWeight:'800', color:'white', margin:'0 0 0.25rem' }}>Create Account</h1>
            <p style={{ color:'#94a3b8', fontSize:'0.8rem' }}>Join your campus navigation system</p>
          </div>

          {/* Error */}
          {error && (
            <div style={{ background:'rgba(239,68,68,0.1)', border:'1px solid rgba(239,68,68,0.3)',
                          borderRadius:'0.75rem', padding:'0.6rem 1rem', marginBottom:'1rem',
                          color:'#fca5a5', fontSize:'0.8rem', display:'flex', gap:'0.4rem' }}>
              <i className="fas fa-exclamation-circle"></i>{error}
            </div>
          )}

          <form onSubmit={submit}>
            <div style={grid2}>
              <div style={wrap}>
                <label style={S.label}>First Name *</label>
                <input style={S.input} placeholder="John" value={form.firstName} onChange={set('firstName')} required
                  onFocus={e=>e.target.style.borderColor='#3b82f6'} onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}/>
              </div>
              <div style={wrap}>
                <label style={S.label}>Last Name</label>
                <input style={S.input} placeholder="Doe" value={form.lastName} onChange={set('lastName')}
                  onFocus={e=>e.target.style.borderColor='#3b82f6'} onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}/>
              </div>
            </div>

            <div style={wrap}>
              <label style={S.label}>Email Address *</label>
              <input type="email" style={S.input} placeholder="you@university.edu" value={form.email} onChange={set('email')} required
                onFocus={e=>e.target.style.borderColor='#3b82f6'} onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}/>
            </div>

            <div style={wrap}>
              <label style={S.label}>Password * (min 8 characters)</label>
              <input type="password" style={S.input} placeholder="••••••••" value={form.password} onChange={set('password')} required
                onFocus={e=>e.target.style.borderColor='#3b82f6'} onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}/>
            </div>

            <div style={grid2}>
              <div style={wrap}>
                <label style={S.label}>Student ID</label>
                <input style={S.input} placeholder="2024CS001" value={form.studentId} onChange={set('studentId')}
                  onFocus={e=>e.target.style.borderColor='#3b82f6'} onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}/>
              </div>
              <div style={wrap}>
                <label style={S.label}>Year of Study</label>
                <select style={S.select} value={form.yearOfStudy} onChange={set('yearOfStudy')}>
                  <option value="">Select year</option>
                  {[1,2,3,4].map(y => <option key={y} value={y}>Year {y}</option>)}
                </select>
              </div>
            </div>

            <div style={wrap}>
              <label style={S.label}>Department</label>
              <input style={S.input} placeholder="Computer Science" value={form.department} onChange={set('department')}
                onFocus={e=>e.target.style.borderColor='#3b82f6'} onBlur={e=>e.target.style.borderColor='rgba(255,255,255,0.12)'}/>
            </div>

            <button type="submit" disabled={loading}
              style={{ width:'100%', padding:'0.875rem', background: loading ? '#1d4ed8':'#2563eb',
                       border:'none', borderRadius:'0.875rem', color:'white', fontSize:'1rem',
                       fontWeight:'700', cursor: loading?'not-allowed':'pointer',
                       transition:'all 0.2s', boxShadow:'0 4px 15px rgba(37,99,235,0.4)', marginTop:'0.5rem' }}
              onMouseOver={e=>{ if(!loading) e.target.style.background='#1d4ed8' }}
              onMouseOut={e=>{ if(!loading) e.target.style.background='#2563eb' }}>
              {loading ? <><i className="fas fa-spinner fa-spin" style={{ marginRight:'0.5rem' }}></i>Creating account...</>
                       : <><i className="fas fa-check" style={{ marginRight:'0.5rem' }}></i>Create Account</>}
            </button>
          </form>

          <div style={{ textAlign:'center', marginTop:'1.25rem', fontSize:'0.875rem' }}>
            <span style={{ color:'#64748b' }}>Already have an account? </span>
            <Link to="/login" style={{ color:'#60a5fa', fontWeight:'600', textDecoration:'none' }}>Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
