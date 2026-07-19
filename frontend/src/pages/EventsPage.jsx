import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { eventApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

export default function EventsPage() {
  const { isAdmin } = useAuth()
  const navigate    = useNavigate()
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ title:'', description:'', venueName:'', organizer:'',
    startTime:'', endTime:'', maxParticipants:'', status:'UPCOMING' })

  const load = () => {
    setLoading(true)
    eventApi.getAll().then(r => setEvents(r.data || [])).catch(() => toast.error('Failed to load events'))
    .finally(() => setLoading(false))
  }
  useEffect(() => { load() }, [])

  const submit = async e => {
    e.preventDefault()
    try {
      await eventApi.create({ ...form, maxParticipants: form.maxParticipants ? Number(form.maxParticipants) : null })
      toast.success('Event created')
      setShowForm(false)
      setForm({ title:'', description:'', venueName:'', organizer:'', startTime:'', endTime:'', maxParticipants:'', status:'UPCOMING' })
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed') }
  }

  const del = async id => {
    if (!window.confirm('Cancel this event?')) return
    try { await eventApi.delete(id); toast.success('Event cancelled'); load() }
    catch { toast.error('Failed') }
  }

  const fmt = dt => dt ? new Date(dt).toLocaleString('en-IN', { dateStyle:'medium', timeStyle:'short' }) : 'TBD'

  const STATUS = { UPCOMING:'bg-blue-500/10 text-blue-400 border-blue-500/20', ONGOING:'bg-green-500/10 text-green-400 border-green-500/20', COMPLETED:'bg-slate-500/10 text-slate-400 border-slate-500/20', CANCELLED:'bg-red-500/10 text-red-400 border-red-500/20' }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <i className="fas fa-calendar-alt text-pink-400"></i> Campus Events
        </h1>
        {isAdmin() && (
          <button onClick={() => setShowForm(!showForm)} className="btn-primary text-sm">
            <i className="fas fa-plus mr-2"></i>Create Event
          </button>
        )}
      </div>

      {showForm && isAdmin() && (
        <div className="card">
          <h2 className="font-semibold text-white mb-4">Create New Event</h2>
          <form onSubmit={submit} className="grid md:grid-cols-2 gap-4">
            {[['title','Event Title *','text',true],['venueName','Venue Name','text'],
              ['organizer','Organizer','text'],['maxParticipants','Max Participants','number']].map(([k,lbl,t,req])=>(
              <div key={k}>
                <label className="text-xs text-slate-400 mb-1 block">{lbl}</label>
                <input className="input text-sm" type={t} required={req}
                  value={form[k]} onChange={e=>setForm({...form,[k]:e.target.value})}/>
              </div>
            ))}
            <div>
              <label className="text-xs text-slate-400 mb-1 block">Start Time *</label>
              <input className="input text-sm" type="datetime-local" required
                value={form.startTime} onChange={e=>setForm({...form,startTime:e.target.value})}/>
            </div>
            <div>
              <label className="text-xs text-slate-400 mb-1 block">End Time *</label>
              <input className="input text-sm" type="datetime-local" required
                value={form.endTime} onChange={e=>setForm({...form,endTime:e.target.value})}/>
            </div>
            <div className="md:col-span-2">
              <label className="text-xs text-slate-400 mb-1 block">Description</label>
              <textarea className="input text-sm h-20 resize-none" value={form.description}
                onChange={e=>setForm({...form,description:e.target.value})} placeholder="Event description..."/>
            </div>
            <div className="md:col-span-2 flex gap-3">
              <button type="submit" className="btn-primary text-sm">Create Event</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-ghost text-sm">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="text-center py-20 text-slate-400">
          <i className="fas fa-spinner fa-spin text-3xl mb-3 block text-blue-400"></i>Loading events...
        </div>
      ) : events.length === 0 ? (
        <div className="text-center py-20 text-slate-500">
          <i className="fas fa-calendar-times text-4xl mb-3 block"></i>No events found
        </div>
      ) : (
        <div className="grid md:grid-cols-2 gap-4">
          {events.map(ev => (
            <div key={ev.id} className="card hover:border-pink-500/20 transition-all space-y-3">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="font-semibold text-white">{ev.title}</div>
                  {ev.organizer && <div className="text-xs text-slate-400 mt-0.5">by {ev.organizer}</div>}
                </div>
                <span className={`text-xs px-2 py-1 rounded-full border shrink-0 ${STATUS[ev.status] || STATUS.UPCOMING}`}>
                  {ev.status}
                </span>
              </div>

              {ev.description && <p className="text-sm text-slate-400 line-clamp-2">{ev.description}</p>}

              <div className="grid grid-cols-2 gap-2 text-xs text-slate-400">
                <span><i className="fas fa-clock mr-1 text-blue-400"></i>{fmt(ev.startTime)}</span>
                <span><i className="fas fa-hourglass-end mr-1 text-purple-400"></i>{fmt(ev.endTime)}</span>
                {ev.venueName && <span className="col-span-2"><i className="fas fa-map-marker-alt mr-1 text-pink-400"></i>{ev.venueName}</span>}
                {ev.maxParticipants && <span><i className="fas fa-users mr-1"></i>Max {ev.maxParticipants}</span>}
              </div>

              <div className="flex gap-2 pt-2 border-t border-white/5">
                <button onClick={() => navigate('/map')} className="btn-ghost text-xs px-3 py-1.5 flex-1">
                  <i className="fas fa-route mr-1"></i>Navigate to Event
                </button>
                {isAdmin() && (
                  <button onClick={() => del(ev.id)}
                    className="px-3 py-1.5 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 text-xs transition-all">
                    <i className="fas fa-times"></i>
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
