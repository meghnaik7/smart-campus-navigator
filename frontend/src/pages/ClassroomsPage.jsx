import React, { useState, useEffect } from 'react'
import { classroomApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

const STATUS_COLORS = {
  AVAILABLE:   'bg-green-500/10 text-green-400 border-green-500/20',
  OCCUPIED:    'bg-red-500/10 text-red-400 border-red-500/20',
  MAINTENANCE: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  CLOSED:      'bg-slate-500/10 text-slate-400 border-slate-500/20',
}

const TYPE_ICONS = {
  LECTURE_HALL:'fa-school', COMPUTER_LAB:'fa-laptop', CHEMISTRY_LAB:'fa-flask',
  PHYSICS_LAB:'fa-atom', MECHANICAL_WORKSHOP:'fa-wrench', SEMINAR_HALL:'fa-users',
  EXAMINATION_HALL:'fa-file-alt', ELECTRONICS_LAB:'fa-microchip', DEFAULT:'fa-door-open',
}

export default function ClassroomsPage() {
  const { isAdmin } = useAuth()
  const [list, setList]     = useState([])
  const [q, setQ]           = useState('')
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    classroomApi.getAll().then(r => setList(r.data || [])).catch(() => toast.error('Failed to load'))
    .finally(() => setLoading(false))
  }
  useEffect(() => { load() }, [])

  const filtered = list.filter(c => {
    const matchQ = !q || c.roomNumber?.toLowerCase().includes(q.toLowerCase()) || c.name?.toLowerCase().includes(q.toLowerCase()) || c.buildingName?.toLowerCase().includes(q.toLowerCase())
    const matchF = filter === 'ALL' || c.availabilityStatus === filter
    return matchQ && matchF
  })

  const setStatus = async (id, status) => {
    try {
      await classroomApi.setAvailability(id, status)
      toast.success(`Status updated to ${status}`)
      load()
    } catch { toast.error('Failed to update status') }
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <i className="fas fa-door-open text-amber-400"></i> Classrooms & Labs
        </h1>
        <div className="flex gap-3 flex-wrap">
          <div className="relative">
            <i className="fas fa-search absolute left-3 top-2.5 text-slate-500"></i>
            <input className="input pl-9 text-sm" placeholder="Search rooms..."
              value={q} onChange={e => setQ(e.target.value)} />
          </div>
          <select className="input text-sm w-auto" value={filter} onChange={e => setFilter(e.target.value)}>
            <option value="ALL">All Status</option>
            {['AVAILABLE','OCCUPIED','MAINTENANCE','CLOSED'].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        {['ALL','AVAILABLE','OCCUPIED','MAINTENANCE'].map(s => {
          const count = s === 'ALL' ? list.length : list.filter(c => c.availabilityStatus === s).length
          return (
            <button key={s} onClick={() => setFilter(s)}
              className={`card text-center cursor-pointer hover:border-blue-500/40 transition-all ${filter===s?'border-blue-500/50':''}`}>
              <div className="text-2xl font-bold text-white">{count}</div>
              <div className="text-xs text-slate-400 mt-1">{s === 'ALL' ? 'Total' : s}</div>
            </button>
          )
        })}
      </div>

      {loading ? (
        <div className="text-center py-20 text-slate-400">
          <i className="fas fa-spinner fa-spin text-3xl mb-3 block text-blue-400"></i>Loading classrooms...
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 text-slate-500">
          <i className="fas fa-door-open text-4xl mb-3 block"></i>No classrooms found
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map(c => (
            <div key={c.id} className="card hover:border-amber-500/20 transition-all space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-amber-500/10 flex items-center justify-center">
                    <i className={`fas ${TYPE_ICONS[c.roomType] || TYPE_ICONS.DEFAULT} text-amber-400`}></i>
                  </div>
                  <div>
                    <div className="font-semibold text-white">{c.roomNumber}</div>
                    <div className="text-xs text-slate-400">{c.roomType?.replace(/_/g,' ')}</div>
                  </div>
                </div>
                <span className={`text-xs px-2 py-1 rounded-full border ${STATUS_COLORS[c.availabilityStatus] || STATUS_COLORS.CLOSED}`}>
                  {c.availabilityStatus}
                </span>
              </div>

              {c.name && <div className="text-sm text-slate-300">{c.name}</div>}

              <div className="grid grid-cols-2 gap-2 text-xs text-slate-400">
                {c.buildingName && <span><i className="fas fa-building mr-1"></i>{c.buildingName}</span>}
                {c.floor != null && <span><i className="fas fa-layer-group mr-1"></i>Floor {c.floor}</span>}
                {c.capacity  && <span><i className="fas fa-users mr-1"></i>{c.capacity} seats</span>}
              </div>

              <div className="flex gap-2 text-xs">
                {c.hasProjector && <span className="glass px-2 py-1 text-blue-400"><i className="fas fa-video mr-1"></i>Projector</span>}
                {c.hasAc       && <span className="glass px-2 py-1 text-cyan-400"><i className="fas fa-wind mr-1"></i>AC</span>}
                {c.hasComputers && <span className="glass px-2 py-1 text-green-400"><i className="fas fa-laptop mr-1"></i>Computers</span>}
              </div>

              {isAdmin() && (
                <div className="pt-2 border-t border-white/5">
                  <label className="text-xs text-slate-500 block mb-1">Update Status:</label>
                  <div className="flex gap-1 flex-wrap">
                    {['AVAILABLE','OCCUPIED','MAINTENANCE'].map(s => (
                      <button key={s} onClick={() => setStatus(c.id, s)}
                        className={`text-xs px-2 py-1 rounded-lg border transition-all ${c.availabilityStatus===s?'border-blue-500 text-blue-300 bg-blue-500/10':'border-white/10 text-slate-400 hover:border-white/20'}`}>
                        {s}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
