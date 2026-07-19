import React, { useState, useEffect } from 'react'
import CampusMap from '../components/CampusMap'
import { nodeApi, buildingApi, facultyApi, classroomApi, navApi, searchApi } from '../services/api'
import toast from 'react-hot-toast'

const card = { background:'rgba(255,255,255,0.05)', border:'1px solid rgba(255,255,255,0.1)',
               borderRadius:'1rem', padding:'1.25rem' }
const inputStyle = { width:'100%', padding:'0.6rem 0.875rem', background:'rgba(255,255,255,0.06)',
                     border:'1px solid rgba(255,255,255,0.12)', borderRadius:'0.6rem',
                     color:'white', fontSize:'0.875rem', outline:'none', boxSizing:'border-box' }
const selectStyle = { ...inputStyle, background:'#1e293b' }

export default function MapPage() {
  const [nodes,     setNodes]     = useState([])
  const [faculty,   setFaculty]   = useState([])
  const [classrooms,setClassrooms]= useState([])
  const [fromNode,  setFromNode]  = useState('')
  const [toNode,    setToNode]    = useState('')
  const [route,     setRoute]     = useState(null)
  const [loading,   setLoading]   = useState(false)
  const [mode,      setMode]      = useState('node')
  const [selFaculty,setSelFaculty]= useState('')
  const [selRoom,   setSelRoom]   = useState('')
  const [nearType,  setNearType]  = useState('CAFETERIA')
  const [selNodeId, setSelNodeId] = useState(null)
  const [suggestions,setSugg]     = useState([])
  const [searchQ,   setSearchQ]   = useState('')

  useEffect(() => {
    nodeApi.getAll().then(r   => setNodes(Array.isArray(r.data)?r.data:[])).catch(()=>{})
    facultyApi.getAll().then(r=> setFaculty(Array.isArray(r.data)?r.data:[])).catch(()=>{})
    classroomApi.getAll().then(r=>setClassrooms(Array.isArray(r.data)?r.data:[])).catch(()=>{})
  }, [])

  const handleSearch = async v => {
    setSearchQ(v)
    if (v.length < 2) { setSugg([]); return }
    try {
      const r = await searchApi.suggest(v, 8)
      setSugg(Array.isArray(r.data)?r.data:[])
    } catch { setSugg([]) }
  }

  const findRoute = async () => {
    if (!fromNode) { toast.error('Select your current location (FROM)'); return }
    setLoading(true); setRoute(null)
    try {
      let res
      if (mode==='node')     { if (!toNode) { toast.error('Select destination'); setLoading(false); return }; res = await navApi.route(fromNode,toNode) }
      else if(mode==='faculty')  { if (!selFaculty) { toast.error('Select faculty'); setLoading(false); return }; res = await navApi.toFaculty(fromNode,selFaculty) }
      else if(mode==='classroom'){ if (!selRoom)    { toast.error('Select room'); setLoading(false); return }; res = await navApi.toClassroom(fromNode,selRoom) }
      else if(mode==='nearest')  { res = await navApi.nearest(fromNode,nearType) }
      if (res?.data?.found) { setRoute(res.data); toast.success(`✅ ${res.data.distanceDisplay} · ${res.data.timeDisplay}`) }
      else toast.error(res?.data?.message||'No route found between selected points')
    } catch(e) { toast.error(e.response?.data?.message||'Navigation failed') }
    finally { setLoading(false) }
  }

  const modeBtn = (m, label, icon) => (
    <button key={m} onClick={() => setMode(m)}
      style={{ flex:1, padding:'0.5rem', borderRadius:'0.5rem', border:'none', cursor:'pointer',
               fontSize:'0.75rem', fontWeight:'600', transition:'all 0.15s',
               background: mode===m ? '#2563eb' : 'rgba(255,255,255,0.05)',
               color: mode===m ? 'white' : '#94a3b8' }}>
      <i className={`fas ${icon}`} style={{ marginRight:'0.25rem' }}></i>{label}
    </button>
  )

  return (
    <div style={{ maxWidth:'1280px', margin:'0 auto', padding:'1.5rem 1rem' }}>
      <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'1rem', flexWrap:'wrap', gap:'0.75rem' }}>
        <h1 style={{ fontSize:'1.5rem', fontWeight:'800', color:'white', display:'flex', alignItems:'center', gap:'0.5rem', margin:0 }}>
          <i className="fas fa-map" style={{ color:'#60a5fa' }}></i> Campus Map
        </h1>
        {route?.found && (
          <div style={{ display:'flex', gap:'0.75rem' }}>
            {[
              ['fa-route','#93c5fd', route.distanceDisplay],
              ['fa-clock','#86efac', route.timeDisplay],
              ['fa-map-pin','#c4b5fd', `${route.nodeCount} stops`]
            ].map(([ic,col,val])=>(
              <span key={ic} style={{ background:'rgba(255,255,255,0.06)', border:'1px solid rgba(255,255,255,0.1)',
                borderRadius:'0.5rem', padding:'0.3rem 0.7rem', fontSize:'0.8rem', color:col, fontWeight:'600' }}>
                <i className={`fas ${ic}`} style={{ marginRight:'0.3rem' }}></i>{val}
              </span>
            ))}
          </div>
        )}
      </div>

      <div style={{ display:'grid', gridTemplateColumns:'300px 1fr', gap:'1rem', alignItems:'start' }}>
        {/* Control Panel */}
        <div style={{ display:'flex', flexDirection:'column', gap:'0.75rem' }}>
          <div style={card}>
            <div style={{ fontWeight:'700', color:'white', marginBottom:'0.875rem', fontSize:'0.9rem' }}>
              <i className="fas fa-location-arrow" style={{ color:'#60a5fa', marginRight:'0.4rem' }}></i>Route Planner
            </div>

            {/* Search */}
            <div style={{ position:'relative', marginBottom:'0.75rem' }}>
              <input style={inputStyle} placeholder="Search campus locations..."
                value={searchQ} onChange={e => handleSearch(e.target.value)} />
              {suggestions.length > 0 && (
                <div style={{ position:'absolute', top:'100%', left:0, right:0, zIndex:30,
                  background:'#1e293b', border:'1px solid rgba(255,255,255,0.1)',
                  borderRadius:'0.6rem', marginTop:'0.25rem', maxHeight:'200px', overflowY:'auto',
                  boxShadow:'0 10px 30px rgba(0,0,0,0.5)' }}>
                  {suggestions.map((s,i) => (
                    <div key={i} onClick={() => { setSearchQ(s.displayText); setSugg([]) }}
                      style={{ padding:'0.5rem 0.75rem', cursor:'pointer', borderBottom:'1px solid rgba(255,255,255,0.05)',
                               display:'flex', gap:'0.5rem', alignItems:'center' }}
                      onMouseOver={e=>e.currentTarget.style.background='rgba(255,255,255,0.08)'}
                      onMouseOut={e=>e.currentTarget.style.background='transparent'}>
                      <i className={`fas ${s.entityType==='BUILDING'?'fa-building':s.entityType==='FACULTY'?'fa-user':s.entityType==='CLASSROOM'?'fa-door-open':'fa-calendar'}`}
                         style={{ color:'#60a5fa', fontSize:'0.75rem', width:'14px' }}></i>
                      <div>
                        <div style={{ fontSize:'0.8rem', color:'white' }}>{s.displayText}</div>
                        <div style={{ fontSize:'0.65rem', color:'#64748b' }}>{s.entityType}</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* FROM */}
            <div style={{ marginBottom:'0.75rem' }}>
              <label style={{ fontSize:'0.75rem', color:'#94a3b8', display:'block', marginBottom:'0.3rem', fontWeight:'600' }}>
                📍 FROM — Your Location
              </label>
              <select style={selectStyle} value={fromNode} onChange={e => setFromNode(e.target.value)}>
                <option value="">Select starting point...</option>
                {nodes.map(n => <option key={n.id} value={n.id}>{n.name}{n.buildingName?` (${n.buildingName})`:''}</option>)}
              </select>
            </div>

            {/* Mode buttons */}
            <div style={{ display:'flex', gap:'0.3rem', marginBottom:'0.75rem' }}>
              {modeBtn('node','Node','fa-map-pin')}
              {modeBtn('faculty','Faculty','fa-user')}
              {modeBtn('classroom','Room','fa-door-open')}
              {modeBtn('nearest','Nearest','fa-search-location')}
            </div>

            {/* TO options */}
            <div style={{ marginBottom:'0.875rem' }}>
              {mode==='node' && (
                <>
                  <label style={{ fontSize:'0.75rem', color:'#94a3b8', display:'block', marginBottom:'0.3rem', fontWeight:'600' }}>🎯 TO — Destination</label>
                  <select style={selectStyle} value={toNode} onChange={e => setToNode(e.target.value)}>
                    <option value="">Select destination...</option>
                    {nodes.map(n => <option key={n.id} value={n.id}>{n.name}{n.buildingName?` (${n.buildingName})`:''}</option>)}
                  </select>
                </>
              )}
              {mode==='faculty' && (
                <>
                  <label style={{ fontSize:'0.75rem', color:'#94a3b8', display:'block', marginBottom:'0.3rem', fontWeight:'600' }}>👨‍🏫 Faculty Member</label>
                  <select style={selectStyle} value={selFaculty} onChange={e => setSelFaculty(e.target.value)}>
                    <option value="">Select faculty...</option>
                    {faculty.map(f => <option key={f.id} value={f.id}>{f.name} — {f.cabinNumber||'N/A'}</option>)}
                  </select>
                </>
              )}
              {mode==='classroom' && (
                <>
                  <label style={{ fontSize:'0.75rem', color:'#94a3b8', display:'block', marginBottom:'0.3rem', fontWeight:'600' }}>🚪 Classroom / Lab</label>
                  <select style={selectStyle} value={selRoom} onChange={e => setSelRoom(e.target.value)}>
                    <option value="">Select room...</option>
                    {classrooms.map(c => <option key={c.id} value={c.id}>{c.roomNumber} — {c.name||c.roomType}</option>)}
                  </select>
                </>
              )}
              {mode==='nearest' && (
                <>
                  <label style={{ fontSize:'0.75rem', color:'#94a3b8', display:'block', marginBottom:'0.3rem', fontWeight:'600' }}>🔍 Nearest Facility</label>
                  <select style={selectStyle} value={nearType} onChange={e => setNearType(e.target.value)}>
                    {['CAFETERIA','WASHROOM','MEDICAL','PARKING','LIBRARY','AUDITORIUM','SPORTS'].map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </>
              )}
            </div>

            <button onClick={findRoute} disabled={loading}
              style={{ width:'100%', padding:'0.75rem', background: loading?'#1d4ed8':'#2563eb', border:'none',
                       borderRadius:'0.75rem', color:'white', fontWeight:'700', cursor:loading?'not-allowed':'pointer',
                       fontSize:'0.9rem', transition:'all 0.2s', marginBottom:'0.5rem' }}
              onMouseOver={e=>{if(!loading)e.target.style.background='#1d4ed8'}}
              onMouseOut={e=>{if(!loading)e.target.style.background='#2563eb'}}>
              {loading ? <><i className="fas fa-spinner fa-spin" style={{ marginRight:'0.4rem' }}></i>Finding route...</>
                       : <><i className="fas fa-route" style={{ marginRight:'0.4rem' }}></i>Find Route</>}
            </button>

            {route?.found && (
              <button onClick={() => { setRoute(null); setSelNodeId(null) }}
                style={{ width:'100%', padding:'0.6rem', background:'rgba(239,68,68,0.1)', border:'1px solid rgba(239,68,68,0.2)',
                         borderRadius:'0.75rem', color:'#fca5a5', cursor:'pointer', fontWeight:'600', fontSize:'0.8rem' }}>
                <i className="fas fa-times" style={{ marginRight:'0.3rem' }}></i>Clear Route
              </button>
            )}
          </div>

          {/* Step-by-step directions */}
          {route?.found && route.path?.length > 0 && (
            <div style={card}>
              <div style={{ fontWeight:'700', color:'white', marginBottom:'0.75rem', fontSize:'0.875rem' }}>
                <i className="fas fa-list-ol" style={{ color:'#60a5fa', marginRight:'0.4rem' }}></i>Directions
              </div>
              <div style={{ maxHeight:'220px', overflowY:'auto', display:'flex', flexDirection:'column', gap:'0.25rem' }}>
                {route.path.map((step, i) => (
                  <div key={i} style={{ display:'flex', alignItems:'center', gap:'0.5rem', padding:'0.4rem 0.6rem',
                    borderRadius:'0.5rem', fontSize:'0.78rem',
                    background: i===0 ? 'rgba(34,197,94,0.1)' : i===route.path.length-1 ? 'rgba(239,68,68,0.1)' : 'rgba(255,255,255,0.03)',
                    color: i===0 ? '#86efac' : i===route.path.length-1 ? '#fca5a5' : '#94a3b8' }}>
                    <span style={{ width:'1.25rem', height:'1.25rem', borderRadius:'50%', background:'rgba(255,255,255,0.08)',
                      display:'flex', alignItems:'center', justifyContent:'center', fontSize:'0.65rem', fontWeight:'700', flexShrink:0, color:'white' }}>
                      {i+1}
                    </span>
                    <span style={{ flex:1 }}>{step.name}</span>
                    {i===0 && <span style={{ fontSize:'0.65rem', fontWeight:'700', color:'#4ade80' }}>START</span>}
                    {i===route.path.length-1 && <span style={{ fontSize:'0.65rem', fontWeight:'700', color:'#f87171' }}>END</span>}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Map */}
        <div>
          <CampusMap route={route} selectedNodeId={selNodeId}
            onNodeClick={n => {
              setSelNodeId(n.id)
              if (!fromNode) {
                setFromNode(String(n.id))
                toast(`📍 FROM set to: ${n.name}`, { icon: '📍' })
              } else if (!toNode && mode === 'node') {
                setToNode(String(n.id))
                toast(`🎯 TO set to: ${n.name}`, { icon: '🎯' })
              } else {
                setFromNode(String(n.id))
                setToNode('')
                toast(`📍 FROM reset to: ${n.name}`, { icon: '📍' })
              }
            }} />
          <p style={{ textAlign:'center', color:'#475569', fontSize:'0.75rem', marginTop:'0.5rem' }}>
            <i className="fas fa-info-circle" style={{ marginRight:'0.3rem' }}></i>
            Click a node to select as FROM · Scroll to zoom · Drag to pan
          </p>
        </div>
      </div>
    </div>
  )
}
