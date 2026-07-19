import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { facultyApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

export default function FacultyPage() {
  const { isAdmin } = useAuth()
  const navigate    = useNavigate()
  const [list, setList]     = useState([])
  const [q, setQ]           = useState('')
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm]     = useState({ name:'', designation:'', email:'', phone:'', specialization:'', cabinNumber:'', floor:0 })

  const load = () => {
    setLoading(true)
    facultyApi.getAll().then(r => setList(r.data || [])).catch(() => toast.error('Failed to load faculty'))
    .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const filtered = list.filter(f =>
    f.name?.toLowerCase().includes(q.toLowerCase()) ||
    f.designation?.toLowerCase().includes(q.toLowerCase()) ||
    f.departmentName?.toLowerCase().includes(q.toLowerCase()) ||
    f.specialization?.toLowerCase().includes(q.toLowerCase())
  )

  const submit = async e => {
    e.preventDefault()
    try {
      await facultyApi.create(form)
      toast.success('Faculty created')
      setShowForm(false)
      setForm({ name:'', designation:'', email:'', phone:'', specialization:'', cabinNumber:'', floor:0 })
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed') }
  }

  const del = async id => {
    if (!window.confirm('Deactivate this faculty member?')) return
    try { await facultyApi.delete(id); toast.success('Faculty deactivated'); load() }
    catch { toast.error('Failed') }
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <i className="fas fa-chalkboard-teacher text-purple-400"></i> Faculty Directory
        </h1>
        <div className="flex gap-3">
          <div className="relative">
            <i className="fas fa-search absolute left-3 top-2.5 text-slate-500"></i>
            <input className="input pl-9 text-sm" placeholder="Search faculty..."
              value={q} onChange={e => setQ(e.target.value)} />
          </div>
          {isAdmin() && (
            <button onClick={() => setShowForm(!showForm)} className="btn-primary text-sm">
              <i className="fas fa-plus mr-2"></i>Add Faculty
            </button>
          )}
        </div>
      </div>

      {/* Add form */}
      {showForm && isAdmin() && (
        <div className="card">
          <h2 className="font-semibold text-white mb-4">Add New Faculty Member</h2>
          <form onSubmit={submit} className="grid md:grid-cols-3 gap-4">
            {[['name','Name *','text',true],['designation','Designation','text'],['email','Email','email'],
              ['phone','Phone','text'],['specialization','Specialization','text'],['cabinNumber','Cabin No.','text']].map(([k,lbl,t,req])=>(
              <div key={k}>
                <label className="text-xs text-slate-400 mb-1 block">{lbl}</label>
                <input className="input text-sm" type={t||'text'} required={req}
                  value={form[k]||''} onChange={e=>setForm({...form,[k]:e.target.value})}/>
              </div>
            ))}
            <div>
              <label className="text-xs text-slate-400 mb-1 block">Floor</label>
              <input className="input text-sm" type="number" min="0" max="10"
                value={form.floor} onChange={e=>setForm({...form,floor:Number(e.target.value)})}/>
            </div>
            <div className="md:col-span-3 flex gap-3">
              <button type="submit" className="btn-primary text-sm">Save Faculty</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-ghost text-sm">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {/* Grid */}
      {loading ? (
        <div className="text-center py-20 text-slate-400">
          <i className="fas fa-spinner fa-spin text-3xl mb-3 block text-blue-400"></i>Loading faculty...
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 text-slate-500">
          <i className="fas fa-user-slash text-4xl mb-3 block"></i>No faculty found
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map(f => (
            <div key={f.id} className="card hover:border-purple-500/30 transition-all space-y-3">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-xl bg-purple-600/20 flex items-center justify-center shrink-0">
                  {f.photoUrl
                    ? <img src={f.photoUrl} alt={f.name} className="w-12 h-12 rounded-xl object-cover"/>
                    : <i className="fas fa-user text-purple-400 text-xl"></i>}
                </div>
                <div>
                  <div className="font-semibold text-white">{f.name}</div>
                  <div className="text-xs text-purple-400">{f.designation}</div>
                </div>
              </div>

              <div className="space-y-1 text-sm">
                {f.departmentName && (
                  <div className="text-slate-300 flex items-center gap-2">
                    <i className="fas fa-building w-4 text-slate-500"></i>{f.departmentName}
                  </div>
                )}
                {f.specialization && (
                  <div className="text-slate-400 flex items-center gap-2">
                    <i className="fas fa-microscope w-4 text-slate-500"></i>{f.specialization}
                  </div>
                )}
                {f.cabinNumber && (
                  <div className="text-slate-400 flex items-center gap-2">
                    <i className="fas fa-door-closed w-4 text-slate-500"></i>
                    Cabin {f.cabinNumber}{f.buildingName ? ` · ${f.buildingName}` : ''}{f.floor != null ? ` · Floor ${f.floor}` : ''}
                  </div>
                )}
                {f.email && (
                  <div className="text-slate-400 flex items-center gap-2">
                    <i className="fas fa-envelope w-4 text-slate-500"></i>{f.email}
                  </div>
                )}
              </div>

              <div className="flex items-center justify-between pt-2 border-t border-white/5">
                <span className={`text-xs px-2 py-1 rounded-full border ${f.isAvailable?'bg-green-500/10 text-green-400 border-green-500/20':'bg-slate-500/10 text-slate-400 border-slate-500/20'}`}>
                  {f.isAvailable ? 'Available' : 'Busy'}
                </span>
                <div className="flex gap-2">
                  <button onClick={() => navigate('/map')}
                    className="text-xs btn-ghost px-3 py-1">
                    <i className="fas fa-route mr-1"></i>Navigate
                  </button>
                  {isAdmin() && (
                    <button onClick={() => del(f.id)}
                      className="text-xs px-2 py-1 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition-all">
                      <i className="fas fa-trash"></i>
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
