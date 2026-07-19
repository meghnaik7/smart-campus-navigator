import React, { useEffect, useRef, useState, useCallback, useMemo } from 'react'
import { nodeApi } from '../services/api'

/* ── Fallback dummy data (used when backend has no nodes yet) ─────────────── */
const DUMMY_NODES = [
  { id:1,  name:'Main Gate',         nodeType:'GATE',              coordinateX:50, coordinateY:92 },
  { id:2,  name:'Parking',           nodeType:'PARKING_LOT',       coordinateX:50, coordinateY:98 },
  { id:3,  name:'Admin Block',       nodeType:'BUILDING_ENTRANCE', coordinateX:50, coordinateY:75 },
  { id:4,  name:'Main Junction',     nodeType:'INTERSECTION',      coordinateX:50, coordinateY:63 },
  { id:5,  name:'CSE Block',         nodeType:'BUILDING_ENTRANCE', coordinateX:22, coordinateY:52 },
  { id:6,  name:'ECE Block',         nodeType:'BUILDING_ENTRANCE', coordinateX:43, coordinateY:52 },
  { id:7,  name:'Cafeteria',         nodeType:'BUILDING_ENTRANCE', coordinateX:70, coordinateY:63 },
  { id:8,  name:'Central Library',   nodeType:'BUILDING_ENTRANCE', coordinateX:50, coordinateY:38 },
  { id:9,  name:'Auditorium',        nodeType:'BUILDING_ENTRANCE', coordinateX:22, coordinateY:22 },
  { id:10, name:'Medical Center',    nodeType:'BUILDING_ENTRANCE', coordinateX:70, coordinateY:22 },
  { id:11, name:'Washroom Block A',  nodeType:'BUILDING_ENTRANCE', coordinateX:33, coordinateY:44 },
  { id:12, name:'South Junction',    nodeType:'INTERSECTION',      coordinateX:50, coordinateY:22 },
]

const DUMMY_EDGES = [
  { id:1,  sourceNodeId:1,  targetNodeId:2  },
  { id:2,  sourceNodeId:1,  targetNodeId:3  },
  { id:3,  sourceNodeId:3,  targetNodeId:4  },
  { id:4,  sourceNodeId:4,  targetNodeId:5  },
  { id:5,  sourceNodeId:4,  targetNodeId:6  },
  { id:6,  sourceNodeId:4,  targetNodeId:7  },
  { id:7,  sourceNodeId:4,  targetNodeId:8  },
  { id:8,  sourceNodeId:5,  targetNodeId:11 },
  { id:9,  sourceNodeId:8,  targetNodeId:12 },
  { id:10, sourceNodeId:12, targetNodeId:9  },
  { id:11, sourceNodeId:12, targetNodeId:10 },
]

const NODE_COLOR = {
  BUILDING_ENTRANCE:'#3b82f6', GATE:'#f59e0b', INTERSECTION:'#10b981',
  STAIRCASE:'#8b5cf6',        ELEVATOR:'#ec4899', OUTDOOR_PATH:'#6b7280',
  PARKING_LOT:'#f97316',      LANDMARK:'#14b8a6', CORRIDOR:'#64748b',
  BUILDING_EXIT:'#ef4444',
}

const BUILDING_BOXES = [
  { label:'CSE Block',   x:8,  y:40, w:28, h:20, color:'rgba(59,130,246,0.07)'  },
  { label:'ECE Block',   x:32, y:40, w:25, h:20, color:'rgba(139,92,246,0.07)'  },
  { label:'Library',     x:37, y:26, w:26, h:18, color:'rgba(20,184,166,0.07)'  },
  { label:'Cafeteria',   x:58, y:50, w:23, h:20, color:'rgba(249,115,22,0.07)'  },
  { label:'Admin Block', x:35, y:62, w:30, h:17, color:'rgba(16,185,129,0.07)'  },
  { label:'Auditorium',  x:8,  y:10, w:26, h:20, color:'rgba(236,72,153,0.07)'  },
  { label:'Medical',     x:58, y:10, w:24, h:18, color:'rgba(239,68,68,0.07)'   },
]

/* ── Main component ─────────────────────────────────────────────────────────── */
export default function CampusMap({ route = null, selectedNodeId = null, onNodeClick = null }) {
  const svgRef = useRef(null)
  const [nodes, setNodes] = useState([])
  const [edges, setEdges] = useState([])
  const [tooltip, setTooltip] = useState(null)
  const [scale,  setScale]  = useState(1)
  const [pan,    setPan]    = useState({ x: 0, y: 0 })
  const dragging  = useRef(false)
  const dragStart = useRef({ mx: 0, my: 0, px: 0, py: 0 })

  /* SVG viewport */
  const W = 720, H = 560

  /* Load nodes + edges from backend; fall back to dummy data */
  useEffect(() => {
    Promise.all([nodeApi.getAll(), nodeApi.getEdges()])
      .then(([nr, er]) => {
        const n = Array.isArray(nr.data) && nr.data.length > 0 ? nr.data : DUMMY_NODES
        const e = Array.isArray(er.data) && er.data.length > 0 ? er.data : DUMMY_EDGES
        setNodes(n)
        setEdges(e)
      })
      .catch(() => { setNodes(DUMMY_NODES); setEdges(DUMMY_EDGES) })
  }, [])

  /* Convert 0-100 % coordinates → SVG pixel space */
  const toSVG = useCallback((px, py) => ({
    x: 20 + (px / 100) * (W - 40),
    y: 20 + (py / 100) * (H - 40),
  }), [W, H])

  /* ── Route data ─────────────────────────────────────────────────── */
  const routePath = route?.path || []

  /*
   * Build a Set of node IDs that are part of the route.
   * Used to highlight the node circles on the route.
   */
  const routeNodeIds = useMemo(() => new Set(routePath.map(p => p.id)), [routePath])

  /*
   * Build a Set of edge keys that are part of the route.
   * An edge is "on route" when BOTH its endpoints appear as
   * consecutive nodes in the route path array.
   * Key format: "minId-maxId"  (order-independent so bidirectional edges match)
   */
  const routeEdgeKeys = useMemo(() => {
    const keys = new Set()
    for (let i = 0; i < routePath.length - 1; i++) {
      const a = routePath[i].id
      const b = routePath[i + 1].id
      keys.add(`${Math.min(a, b)}-${Math.max(a, b)}`)
    }
    return keys
  }, [routePath])

  /* Polyline points string for the animated route overlay */
  const routePolylinePoints = useMemo(() => {
    if (routePath.length < 2) return ''
    return routePath
      .map(node => {
        // The route API returns x,y (same as coordinateX/Y, 0-100 range)
        const px = node.x  ?? node.coordinateX ?? 50
        const py = node.y  ?? node.coordinateY ?? 50
        const { x, y } = toSVG(px, py)
        return `${x},${y}`
      })
      .join(' ')
  }, [routePath, toSVG])

  /* Approximate total polyline length for dash animation */
  const polylineLength = useMemo(() => {
    if (routePath.length < 2) return 0
    let len = 0
    for (let i = 1; i < routePath.length; i++) {
      const ax = routePath[i-1].x  ?? routePath[i-1].coordinateX ?? 50
      const ay = routePath[i-1].y  ?? routePath[i-1].coordinateY ?? 50
      const bx = routePath[i].x    ?? routePath[i].coordinateX   ?? 50
      const by = routePath[i].y    ?? routePath[i].coordinateY   ?? 50
      const { x: x1, y: y1 } = toSVG(ax, ay)
      const { x: x2, y: y2 } = toSVG(bx, by)
      len += Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)
    }
    return Math.ceil(len) + 20  // small buffer
  }, [routePath, toSVG])

  /* ── Pan / zoom handlers ─────────────────────────────────────────── */
  const onWheel = e => {
    e.preventDefault()
    setScale(s => Math.min(4, Math.max(0.35, s * (e.deltaY < 0 ? 1.12 : 0.9))))
  }
  const onMouseDown = e => {
    dragging.current = true
    dragStart.current = { mx: e.clientX, my: e.clientY, px: pan.x, py: pan.y }
  }
  const onMouseMove = e => {
    if (!dragging.current) return
    setPan({
      x: dragStart.current.px + (e.clientX - dragStart.current.mx),
      y: dragStart.current.py + (e.clientY - dragStart.current.my),
    })
  }
  const onMouseUp = () => { dragging.current = false }

  /* ── Tooltip handlers ────────────────────────────────────────────── */
  const showTooltip = (node, e) => {
    if (!svgRef.current) return
    const rect = svgRef.current.getBoundingClientRect()
    setTooltip({
      name: node.name,
      type: node.nodeType,
      bld:  node.buildingName,
      x:    e.clientX - rect.left + 14,
      y:    e.clientY - rect.top  - 10,
    })
  }

  /* ── Render ──────────────────────────────────────────────────────── */
  return (
    <div style={{
      position: 'relative', width: '100%', height: '540px',
      borderRadius: '1rem', overflow: 'hidden',
      border: '1px solid rgba(255,255,255,0.1)', background: '#0f172a',
    }}>

      {/* ── Toolbar ── */}
      <div style={{
        position: 'absolute', top: '0.75rem', right: '0.75rem', zIndex: 10,
        display: 'flex', flexDirection: 'column', gap: '0.35rem',
      }}>
        {[
          ['fa-plus',    'Zoom In',  () => setScale(s => Math.min(4, s * 1.2))],
          ['fa-minus',   'Zoom Out', () => setScale(s => Math.max(0.35, s / 1.2))],
          ['fa-expand',  'Reset',    () => { setScale(1); setPan({ x: 0, y: 0 }) }],
        ].map(([ic, lbl, fn]) => (
          <button key={lbl} onClick={fn} title={lbl} style={{
            width: '2.1rem', height: '2.1rem', borderRadius: '0.45rem',
            background: 'rgba(255,255,255,0.07)', border: '1px solid rgba(255,255,255,0.12)',
            color: '#cbd5e1', cursor: 'pointer', fontSize: '0.75rem',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <i className={`fas ${ic}`}></i>
          </button>
        ))}
      </div>

      {/* ── Route info bar ── */}
      {route?.found && (
        <div style={{
          position: 'absolute', top: '0.75rem', left: '0.75rem', zIndex: 10,
          display: 'flex', gap: '0.75rem',
          background: 'rgba(15,23,42,0.9)', border: '1px solid rgba(59,130,246,0.4)',
          borderRadius: '0.75rem', padding: '0.45rem 0.875rem',
          backdropFilter: 'blur(10px)',
        }}>
          <span style={{ color: '#93c5fd', fontSize: '0.82rem', fontWeight: 700 }}>
            <i className="fas fa-route" style={{ marginRight: '0.35rem' }}></i>
            {route.distanceDisplay}
          </span>
          <span style={{ color: '#86efac', fontSize: '0.82rem', fontWeight: 700 }}>
            <i className="fas fa-clock" style={{ marginRight: '0.35rem' }}></i>
            {route.timeDisplay}
          </span>
          <span style={{ color: '#c4b5fd', fontSize: '0.82rem' }}>
            {route.nodeCount} stops
          </span>
        </div>
      )}

      {/* ── Legend ── */}
      <div style={{
        position: 'absolute', bottom: '0.75rem', left: '0.75rem', zIndex: 10,
        background: 'rgba(15,23,42,0.88)', border: '1px solid rgba(255,255,255,0.1)',
        borderRadius: '0.75rem', padding: '0.5rem 0.75rem', backdropFilter: 'blur(10px)',
      }}>
        {[
          ['#3b82f6', 'Building'],
          ['#f59e0b', 'Gate'],
          ['#10b981', 'Junction'],
          ['#f97316', 'Parking'],
        ].map(([c, l]) => (
          <div key={l} style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.2rem' }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: c, display: 'inline-block' }}></span>
            <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>{l}</span>
          </div>
        ))}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.3rem', borderTop: '1px solid rgba(255,255,255,0.07)', paddingTop: '0.3rem' }}>
          <span style={{ width: 20, height: 3, background: 'rgba(71,85,105,0.7)', display: 'inline-block' }}></span>
          <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>Path</span>
        </div>
        {route?.found && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.2rem' }}>
            <span style={{ width: 20, height: 4, background: '#3b82f6', display: 'inline-block', borderRadius: 2 }}></span>
            <span style={{ fontSize: '0.7rem', color: '#93c5fd', fontWeight: 700 }}>Route</span>
          </div>
        )}
      </div>

      {/* ── Tooltip ── */}
      {tooltip && (
        <div style={{
          position: 'absolute', zIndex: 30, pointerEvents: 'none',
          left: Math.min(tooltip.x, W - 160), top: Math.max(tooltip.y - 40, 4),
          background: 'rgba(15,23,42,0.97)', border: '1px solid rgba(255,255,255,0.13)',
          borderRadius: '0.6rem', padding: '0.45rem 0.7rem', backdropFilter: 'blur(10px)',
        }}>
          <div style={{ fontWeight: 700, color: 'white', fontSize: '0.8rem' }}>{tooltip.name}</div>
          {tooltip.bld && <div style={{ color: '#64748b', fontSize: '0.7rem' }}>{tooltip.bld}</div>}
          <div style={{ color: '#475569', fontSize: '0.65rem', marginTop: '0.1rem' }}>
            {tooltip.type?.replace(/_/g, ' ')}
          </div>
        </div>
      )}

      {/* ── SVG Map ── */}
      <svg
        ref={svgRef}
        width="100%" height="100%"
        viewBox={`0 0 ${W} ${H}`}
        preserveAspectRatio="xMidYMid meet"
        style={{ cursor: dragging.current ? 'grabbing' : 'grab', display: 'block' }}
        onMouseDown={onMouseDown}
        onMouseMove={onMouseMove}
        onMouseUp={onMouseUp}
        onMouseLeave={onMouseUp}
        onWheel={onWheel}
      >
        <defs>
          {/* Grid pattern */}
          <pattern id="cmap-grid" width="40" height="40" patternUnits="userSpaceOnUse">
            <path d="M 40 0 L 0 0 0 40" fill="none" stroke="rgba(255,255,255,0.02)" strokeWidth="0.5"/>
          </pattern>
          {/* Glow filter for route */}
          <filter id="route-glow" x="-40%" y="-40%" width="180%" height="180%">
            <feGaussianBlur stdDeviation="4" result="blur"/>
            <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
          </filter>
          {/* Glow for start/end nodes */}
          <filter id="node-glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="3" result="blur"/>
            <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
          </filter>
        </defs>

        {/* Background */}
        <rect width={W} height={H} fill="#0f172a"/>
        <rect width={W} height={H} fill="url(#cmap-grid)"/>

        {/* Everything inside this group is pan+zoom-able */}
        <g transform={`translate(${pan.x},${pan.y}) scale(${scale})`}
           style={{ transformOrigin: `${W/2}px ${H/2}px` }}>

          {/* Campus boundary box */}
          <rect x={10} y={8} width={W - 20} height={H - 16} rx={14}
            fill="rgba(30,41,59,0.55)" stroke="rgba(255,255,255,0.05)" strokeWidth="1"/>

          {/* Building area boxes */}
          {BUILDING_BOXES.map(b => {
            const sx = 20 + (b.x / 100) * (W - 40)
            const sy = 20 + (b.y / 100) * (H - 40)
            const sw = (b.w  / 100) * (W - 40)
            const sh = (b.h  / 100) * (H - 40)
            return (
              <g key={b.label}>
                <rect x={sx} y={sy} width={sw} height={sh} rx={8}
                  fill={b.color} stroke="rgba(255,255,255,0.06)" strokeWidth="1"/>
                <text x={sx + sw / 2} y={sy + sh / 2 + 4} textAnchor="middle"
                  fill="rgba(255,255,255,0.18)" fontSize="10" fontWeight="600">
                  {b.label}
                </text>
              </g>
            )
          })}

          {/* ── All campus paths (grey dashed) ── */}
          {edges.map(edge => {
            const src = nodes.find(n => n.id === edge.sourceNodeId)
            const tgt = nodes.find(n => n.id === edge.targetNodeId)
            if (!src || !tgt) return null

            const s = toSVG(src.coordinateX, src.coordinateY)
            const t = toSVG(tgt.coordinateX, tgt.coordinateY)

            /* Is this edge part of the selected route? */
            const eKey = `${Math.min(src.id, tgt.id)}-${Math.max(src.id, tgt.id)}`
            const onRoute = routeEdgeKeys.has(eKey)

            return (
              <line key={edge.id}
                x1={s.x} y1={s.y} x2={t.x} y2={t.y}
                stroke={onRoute ? 'rgba(59,130,246,0.25)' : 'rgba(71,85,105,0.45)'}
                strokeWidth={onRoute ? 3 : 1.5}
                strokeDasharray={onRoute ? 'none' : '7,5'}
              />
            )
          })}

          {/* ── Route overlay: single animated polyline ─────────────────
               This draws the FULL route path in one stroke so EVERY
               segment from start → end is visible and animated together.
          ─────────────────────────────────────────────────────────────── */}
          {routePolylinePoints && polylineLength > 0 && (
            <>
              {/* Glow shadow (thicker, blurred) */}
              <polyline
                key={`route-glow-${routePolylinePoints}`}
                points={routePolylinePoints}
                fill="none"
                stroke="#60a5fa"
                strokeWidth="10"
                strokeLinecap="round"
                strokeLinejoin="round"
                opacity="0.25"
                filter="url(#route-glow)"
              />
              {/* Main animated route line */}
              <polyline
                key={`route-line-${routePolylinePoints}`}
                points={routePolylinePoints}
                fill="none"
                stroke="#3b82f6"
                strokeWidth="5"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeDasharray={polylineLength}
                strokeDashoffset={polylineLength}
              >
                <animate
                  attributeName="strokeDashoffset"
                  from={polylineLength}
                  to={0}
                  dur="1.4s"
                  fill="freeze"
                  calcMode="spline"
                  keyTimes="0;1"
                  keySplines="0.4 0 0.2 1"
                />
              </polyline>
              {/* Animated travelling dot along the route */}
              {routePolylinePoints && (
                <circle r="6" fill="#60a5fa" opacity="0.95" filter="url(#node-glow)">
                  <animateMotion
                    dur="1.4s"
                    fill="freeze"
                    calcMode="spline"
                    keyTimes="0;1"
                    keySplines="0.4 0 0.2 1"
                    path={`M ${routePolylinePoints.split(' ').join(' L ')}`}
                  />
                </circle>
              )}
              {/* Hidden path element — not needed anymore */}
            </>
          )}

          {/* ── Node circles ── */}
          {nodes.map(node => {
            const { x, y } = toSVG(node.coordinateX, node.coordinateY)
            const isStart    = routePath.length > 0 && routePath[0].id === node.id
            const isEnd      = routePath.length > 0 && routePath[routePath.length - 1].id === node.id
            const onRoute    = routeNodeIds.has(node.id)
            const isSelected = node.id === selectedNodeId
            const color      = NODE_COLOR[node.nodeType] || '#3b82f6'
            const r          = isStart || isEnd ? 11 : isSelected ? 10 : onRoute ? 9 : 7

            return (
              <g key={node.id}
                 style={{ cursor: onNodeClick ? 'pointer' : 'default' }}
                 onClick={e => { e.stopPropagation(); onNodeClick && onNodeClick(node) }}
                 onMouseEnter={e => showTooltip(node, e)}
                 onMouseLeave={() => setTooltip(null)}>

                {/* Pulse ring for route/selected nodes */}
                {(onRoute || isSelected) && (
                  <circle cx={x} cy={y} r={r + 7} fill={color} opacity="0.12">
                    <animate attributeName="r"
                      values={`${r+4};${r+12};${r+4}`} dur="2s" repeatCount="indefinite"/>
                    <animate attributeName="opacity"
                      values="0.18;0.05;0.18" dur="2s" repeatCount="indefinite"/>
                  </circle>
                )}

                {/* Main circle */}
                <circle cx={x} cy={y} r={r} fill={color}
                  stroke={
                    isStart    ? '#22c55e' :
                    isEnd      ? '#ef4444' :
                    isSelected ? '#ffffff' :
                    onRoute    ? '#93c5fd' :
                    'rgba(255,255,255,0.25)'
                  }
                  strokeWidth={isStart || isEnd || isSelected ? 3 : onRoute ? 2 : 1.5}
                  filter={isStart || isEnd ? 'url(#node-glow)' : ''}
                />

                {/* Inner dot */}
                <circle cx={x} cy={y} r={r * 0.32} fill="rgba(255,255,255,0.55)"/>

                {/* Label: always show for start / end */}
                {(isStart || isEnd) && (
                  <text x={x} y={y - r - 6} textAnchor="middle"
                    fill={isStart ? '#4ade80' : '#f87171'}
                    fontSize="11" fontWeight="800"
                    style={{ filter: 'drop-shadow(0 1px 3px rgba(0,0,0,0.9))' }}>
                    {node.name}
                  </text>
                )}

                {/* Label for intermediate route nodes */}
                {onRoute && !isStart && !isEnd && (
                  <text x={x} y={y - r - 4} textAnchor="middle"
                    fill="#93c5fd" fontSize="9"
                    style={{ filter: 'drop-shadow(0 1px 2px rgba(0,0,0,0.9))' }}>
                    {node.name}
                  </text>
                )}

                {/* Label for non-route nodes (gates & parking always labelled) */}
                {!onRoute && !isSelected &&
                 (node.nodeType === 'GATE' || node.nodeType === 'PARKING_LOT') && (
                  <text x={x} y={y - r - 4} textAnchor="middle"
                    fill="rgba(255,255,255,0.4)" fontSize="9">
                    {node.name}
                  </text>
                )}
              </g>
            )
          })}

          {/* ── Step numbers on route nodes ── */}
          {routePath.map((node, i) => {
            const px = node.x  ?? node.coordinateX ?? 50
            const py = node.y  ?? node.coordinateY ?? 50
            const { x, y } = toSVG(px, py)
            const isStart = i === 0
            const isEnd   = i === routePath.length - 1
            if (isStart || isEnd) return null   // already labelled
            return (
              <g key={`step-${node.id}`}>
                <circle cx={x + 10} cy={y - 10} r={7}
                  fill="#1d4ed8" stroke="#60a5fa" strokeWidth="1.5"/>
                <text x={x + 10} y={y - 10 + 3.5} textAnchor="middle"
                  fill="white" fontSize="8" fontWeight="800">{i + 1}</text>
              </g>
            )
          })}

        </g>{/* end pan+zoom group */}
      </svg>

      <style>{`
        @keyframes pulse-node {
          0%,100% { opacity:0.18; r:8; }
          50%      { opacity:0.05; r:16; }
        }
      `}</style>
    </div>
  )
}
