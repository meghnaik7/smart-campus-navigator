import React, { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { searchApi, buildingApi, facultyApi, classroomApi } from '../services/api'
import toast from 'react-hot-toast'

export default function SearchPage() {
  const [q, setQ]               = useState('')
  const [suggestions, setSugg]  = useState([])
  const [tab, setTab]           = useState('all')
  const [buildings, setBuildings] = useState([])
  const [faculty, setFaculty]   = useState([])
  const [classrooms, setClassrooms] = useState([])
  const [loading, setLoading]   = useState(false)
  const inputRef = useRef(null)
  const navigate = useNavigate()

  useEffect(() => { inputRef.current?.focus() }, [])

  const handleQ = async val => {
    setQ(val)
    if (val.length < 2) { setSugg([]); return }
    try {
      const r = await searchApi.suggest(val, 10)
      setSugg(r.data || [])
    } catch { setSugg([]) }
  }

  const doSearch = async () => {
    if (!q.trim()) return
    setLoading(true); setSugg([])
    try {
      const [b, f, c] = await Promise.all([
        buildingApi.search(q).catch(() => ({ data: { content: [] } })),
        facultyApi.search(q).catch(() => ({ data: { content: [] } })),
        classroomApi.search(q).catch(() => ({ data: { content: [] } })),
      ])
      setBuildings(b.data?.content || b.data || [])
      setFaculty(f.data?.content || f.data || [])
      setClassrooms(c.data?.content || c.data || [])
    } catch { toast.error('Search failed') }
    finally { setLoading(false) }
  }

  const entityIcon = type => ({
    BUILDING:'fa-building text-blue-400', FACULTY:'fa-user text-green-400',
    CLASSROOM:'fa-door-open text-amber-400', EVENT:'fa-calendar text-pink-400'
  }[type] || 'fa-circle text-slate-400')

  const ResultCard = ({ icon, title, sub1, sub2, badge, onNavigate }) => (
    <div className="card hover:border-blue-500/30 transition-all flex items-center justify-between gap-3">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center shrink-0">
          <i className={`fas ${icon} text-lg`}></i>
        </div>
        <div>
          <div className="font-medium text-white">{title}</div>
          <div className="text-xs text-slate-400">{sub1}</div>
          {sub2 && <div className="text-xs text-slate-500">{sub2}</div>}
        </div>
      </div>
      <div className="flex items-center gap-2 shrink-0">
        {badge && <span className="text-xs bg-white/5 border border-white/10 text-slate-300 px-2 py-1 rounded-lg">{badge}</span>}
        {onNavigate && (
          <button onClick={onNavigate} className="btn-primary text-xs px-3 py-1.5">
            <i className="fas fa-route mr-1"></i>Navigate
          </button>
        )}
      </div>
    </div>
  )

  const allCount = buildings.length + faculty.length + classrooms.length

  return (
    <div className="max-w-4xl mx-auto px-4 py-8 space-y-6">
      <h1 className="text-2xl font-bold text-white flex items-center gap-2">
        <i className="fas fa-search text-blue-400"></i> Campus Search
      </h1>

      {/* Search input */}
      <div className="relative">
        <i className="fas fa-search absolute left-4 top-3.5 text-slate-400 text-lg"></i>
        <input ref={inputRef} className="input pl-12 pr-16 py-3 text-lg"
          placeholder="Search buildings, faculty, classrooms, labs..."
          value={q} onChange={e => handleQ(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && doSearch()} />
        <button onClick={doSearch} className="absolute right-2 top-2 btn-primary px-4 py-2">
          {loading ? <i className="fas fa-spinner fa-spin"></i> : 'Search'}
        </button>

        {/* Suggestions dropdown */}
        {suggestions.length > 0 && (
          <div className="absolute top-full left-0 right-0 z-30 bg-slate-800 border border-white/10 rounded-xl mt-1 shadow-2xl overflow-hidden">
            {suggestions.map((s, i) => (
              <div key={i} onClick={() => { setQ(s.displayText); setSugg([]); doSearch() }}
                className="flex items-center gap-3 px-4 py-2.5 hover:bg-white/10 cursor-pointer border-b border-white/5 last:border-0">
                <i className={`fas ${entityIcon(s.entityType)} w-4`}></i>
                <div>
                  <div className="text-sm text-white">{s.displayText}</div>
                  <div className="text-xs text-slate-400">{s.entityType} {s.subtitle ? `· ${s.subtitle}` : ''}</div>
                </div>
                <i className="fas fa-arrow-up-right text-slate-600 ml-auto text-xs"></i>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Tabs */}
      {allCount > 0 && (
        <div className="flex gap-2 flex-wrap">
          {[['all','All',allCount],['buildings','Buildings',buildings.length],['faculty','Faculty',faculty.length],['classrooms','Classrooms',classrooms.length]].map(([t,l,c]) => (
            <button key={t} onClick={() => setTab(t)}
              className={`px-4 py-1.5 rounded-xl text-sm font-medium transition-all ${tab===t?'bg-blue-600 text-white':'btn-ghost'}`}>
              {l} {c > 0 && <span className="ml-1 bg-white/10 px-1.5 py-0.5 rounded-full text-xs">{c}</span>}
            </button>
          ))}
        </div>
      )}

      {/* Results */}
      {allCount === 0 && q.length > 1 && !loading && (
        <div className="text-center py-16 text-slate-500">
          <i className="fas fa-search text-4xl mb-3 block"></i>
          <div className="text-lg">No results found for "{q}"</div>
          <div className="text-sm mt-1">Try a different keyword</div>
        </div>
      )}

      <div className="space-y-3">
        {(tab === 'all' || tab === 'buildings') && buildings.map(b => (
          <ResultCard key={b.id} icon="fa-building text-blue-400"
            title={b.name} sub1={b.type?.replace(/_/g,' ')} sub2={b.description}
            badge={`${b.floors || 1} floor${b.floors !== 1 ? 's' : ''}`}
            onNavigate={() => navigate('/map')} />
        ))}
        {(tab === 'all' || tab === 'faculty') && faculty.map(f => (
          <ResultCard key={f.id} icon="fa-user text-green-400"
            title={f.name} sub1={`${f.designation || ''} · ${f.departmentName || ''}`}
            sub2={`Cabin: ${f.cabinNumber || 'N/A'} · Floor ${f.floor || 0}`}
            badge={f.isAvailable ? 'Available' : 'Busy'}
            onNavigate={() => navigate('/map')} />
        ))}
        {(tab === 'all' || tab === 'classrooms') && classrooms.map(c => (
          <ResultCard key={c.id} icon="fa-door-open text-amber-400"
            title={`${c.roomNumber} — ${c.name || ''}`}
            sub1={`${c.roomType?.replace(/_/g,' ')} · ${c.buildingName || ''}`}
            sub2={`Capacity: ${c.capacity || 'N/A'} · Floor ${c.floor || 0}`}
            badge={c.availabilityStatus}
            onNavigate={() => navigate('/map')} />
        ))}
      </div>
    </div>
  )
}
