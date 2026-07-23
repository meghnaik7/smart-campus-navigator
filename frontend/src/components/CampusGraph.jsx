import React from 'react'

// Converts 0-100 coordinate percentage to SVG pixel position
const W = 700, H = 500
const toX = x => 20 + (x / 100) * (W - 40)
const toY = y => 20 + (y / 100) * (H - 40)

export default function CampusGraph({ nodes, edges, routePath }) {
  // Set of node IDs that are on the current route
  const routeIds = new Set(routePath.map(n => n.id))

  // Set of edge keys that are on the route (used to highlight route edges)
  const routeEdgeKeys = new Set()
  for (let i = 0; i < routePath.length - 1; i++) {
    const a = Math.min(routePath[i].id, routePath[i + 1].id)
    const b = Math.max(routePath[i].id, routePath[i + 1].id)
    routeEdgeKeys.add(`${a}-${b}`)
  }

  const startId = routePath[0]?.id
  const endId   = routePath[routePath.length - 1]?.id

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-700 overflow-hidden">
      <svg width="100%" viewBox={`0 0 ${W} ${H}`} className="block">

        {/* Campus background */}
        <rect width={W} height={H} fill="#111827" />
        <rect x={10} y={10} width={W-20} height={H-20} rx={12}
          fill="#1f2937" stroke="#374151" strokeWidth={1} />

        {/* All edges — grey dashed */}
        {edges.map(e => {
          const src = nodes.find(n => n.id === e.sourceNodeId)
          const tgt = nodes.find(n => n.id === e.targetNodeId)
          if (!src || !tgt) return null
          const key = `${Math.min(src.id, tgt.id)}-${Math.max(src.id, tgt.id)}`
          const onRoute = routeEdgeKeys.has(key)
          return (
            <line key={e.id}
              x1={toX(src.coordinateX)} y1={toY(src.coordinateY)}
              x2={toX(tgt.coordinateX)} y2={toY(tgt.coordinateY)}
              stroke={onRoute ? '#3b82f6' : '#374151'}
              strokeWidth={onRoute ? 4 : 1.5}
              strokeDasharray={onRoute ? 'none' : '6 4'}
            />
          )
        })}

        {/* Distance labels on route edges */}
        {edges.map(e => {
          const src = nodes.find(n => n.id === e.sourceNodeId)
          const tgt = nodes.find(n => n.id === e.targetNodeId)
          if (!src || !tgt) return null
          const key = `${Math.min(src.id, tgt.id)}-${Math.max(src.id, tgt.id)}`
          if (!routeEdgeKeys.has(key)) return null
          const mx = (toX(src.coordinateX) + toX(tgt.coordinateX)) / 2
          const my = (toY(src.coordinateY) + toY(tgt.coordinateY)) / 2
          return (
            <g key={`lbl-${e.id}`}>
              <rect x={mx - 18} y={my - 9} width={36} height={16} rx={4} fill="#1e40af" />
              <text x={mx} y={my + 4} textAnchor="middle" fill="white" fontSize={9} fontWeight="bold">
                {e.distanceMeters}m
              </text>
            </g>
          )
        })}

        {/* Node circles */}
        {nodes.map(node => {
          const cx = toX(node.coordinateX)
          const cy = toY(node.coordinateY)
          const isStart = node.id === startId
          const isEnd   = node.id === endId
          const onRoute = routeIds.has(node.id)
          const r = isStart || isEnd ? 10 : onRoute ? 8 : 6
          const fill  = isStart ? '#22c55e' : isEnd ? '#ef4444' : onRoute ? '#3b82f6' : '#4b5563'
          const stroke= isStart || isEnd ? 'white' : onRoute ? '#93c5fd' : '#6b7280'

          return (
            <g key={node.id}>
              {/* Pulse ring for route nodes */}
              {onRoute && (
                <circle cx={cx} cy={cy} r={r + 6} fill={fill} opacity={0.2} />
              )}
              <circle cx={cx} cy={cy} r={r} fill={fill} stroke={stroke} strokeWidth={1.5} />
              {/* Node label */}
              <text x={cx} y={cy - r - 4} textAnchor="middle"
                fill={onRoute ? '#e5e7eb' : '#9ca3af'} fontSize={9}>
                {node.name.length > 14 ? node.name.slice(0, 13) + '...' : node.name}
              </text>
            </g>
          )
        })}

        {/* Step numbers on route */}
        {routePath.map((node, i) => {
          const cx = toX(node.x ?? node.coordinateX ?? 50)
          const cy = toY(node.y ?? node.coordinateY ?? 50)
          if (i === 0 || i === routePath.length - 1) return null
          return (
            <g key={`step-${node.id}`}>
              <circle cx={cx + 8} cy={cy - 8} r={7} fill="#1d4ed8" stroke="#60a5fa" strokeWidth={1} />
              <text x={cx + 8} y={cy - 4} textAnchor="middle" fill="white" fontSize={8} fontWeight="bold">
                {i}
              </text>
            </g>
          )
        })}

      </svg>

      {/* Legend */}
      <div className="flex gap-4 px-4 py-2 bg-gray-800 text-xs text-gray-400 border-t border-gray-700">
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-green-500 inline-block"></span>Start</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-red-500 inline-block"></span>End</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-blue-500 inline-block"></span>Route node</span>
        <span className="flex items-center gap-1"><span className="w-3 h-0.5 bg-blue-400 inline-block"></span>Route path</span>
        <span className="flex items-center gap-1"><span className="w-3 h-0.5 bg-gray-600 inline-block border-dashed border border-gray-600"></span>Other path</span>
      </div>
    </div>
  )
}
