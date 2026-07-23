import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getNodes, getEdges, addNode, delNode, addEdge, delEdge, reloadGraph } from '../api'

// Reusable small button
const Btn = ({ onClick, children, color = 'blue', disabled }) => {
  const colors = {
    blue:  'bg-blue-600 hover:bg-blue-500',
    red:   'bg-red-700 hover:bg-red-600',
    green: 'bg-green-700 hover:bg-green-600',
  }
  return (
    <button onClick={onClick} disabled={disabled}
      className={`${colors[color]} disabled:opacity-40 text-white text-sm font-medium px-3 py-1.5 rounded-lg transition-colors`}>
      {children}
    </button>
  )
}

const NODE_TYPES = [
  'BUILDING_ENTRANCE','BUILDING_EXIT','INTERSECTION',
  'STAIRCASE','ELEVATOR','CORRIDOR','OUTDOOR_PATH','PARKING_LOT','GATE','LANDMARK'
]

export default function AdminPage() {
  const nav = useNavigate()

  const [nodes, setNodes]   = useState([])
  const [edges, setEdges]   = useState([])
  const [msg,   setMsg]     = useState({ text: '', ok: true })
  const [tab,   setTab]     = useState('nodes')  // 'nodes' or 'edges'

  // New node form state
  const [nodeName, setNodeName]   = useState('')
  const [nodeType, setNodeType]   = useState('INTERSECTION')
  const [nodeX,    setNodeX]      = useState('')
  const [nodeY,    setNodeY]      = useState('')

  // New edge form state
  const [srcId,  setSrcId]  = useState('')
  const [tgtId,  setTgtId]  = useState('')
  const [dist,   setDist]   = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    try {
      const [n, e] = await Promise.all([getNodes(), getEdges()])
      setNodes(n); setEdges(e)
    } catch (err) { showMsg(err.message, false) }
  }

  function showMsg(text, ok = true) {
    setMsg({ text, ok })
    setTimeout(() => setMsg({ text: '', ok: true }), 3000)
  }

  async function handleAddNode(e) {
    e.preventDefault()
    try {
      await addNode({ name: nodeName, nodeType, coordinateX: +nodeX, coordinateY: +nodeY })
      await reloadGraph()
      showMsg(`Node "${nodeName}" added`)
      setNodeName(''); setNodeX(''); setNodeY('')
      load()
    } catch (err) { showMsg(err.message, false) }
  }

  async function handleDelNode(id, name) {
    if (!window.confirm(`Delete node "${name}"?`)) return
    try {
      await delNode(id)
      await reloadGraph()
      showMsg(`Node "${name}" deleted`)
      load()
    } catch (err) { showMsg(err.message, false) }
  }

  async function handleAddEdge(e) {
    e.preventDefault()
    try {
      await addEdge({ sourceNodeId: +srcId, targetNodeId: +tgtId, distanceMeters: +dist })
      await reloadGraph()
      showMsg('Edge added')
      setSrcId(''); setTgtId(''); setDist('')
      load()
    } catch (err) { showMsg(err.message, false) }
  }

  async function handleDelEdge(id) {
    if (!window.confirm('Delete this edge?')) return
    try {
      await delEdge(id)
      await reloadGraph()
      showMsg('Edge deleted')
      load()
    } catch (err) { showMsg(err.message, false) }
  }

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">

      {/* Navbar */}
      <nav className="bg-gray-900 border-b border-gray-700 px-6 py-3 flex items-center justify-between">
        <span className="font-bold text-white">Admin Panel</span>
        <div className="flex gap-3">
          <Btn onClick={() => nav('/')}>Back to Map</Btn>
          <Btn onClick={() => { localStorage.clear(); nav('/login') }} color="red">Logout</Btn>
        </div>
      </nav>

      {/* Status message */}
      {msg.text && (
        <div className={`mx-4 mt-4 px-4 py-3 rounded-lg text-sm border ${
          msg.ok
            ? 'bg-green-900/40 border-green-500/50 text-green-300'
            : 'bg-red-900/40 border-red-500/50 text-red-300'
        }`}>
          {msg.text}
        </div>
      )}

      <div className="max-w-5xl mx-auto px-4 py-6 space-y-6">

        {/* Stats */}
        <div className="grid grid-cols-2 gap-4">
          <div className="bg-gray-900 border border-gray-700 rounded-xl p-4 text-center">
            <div className="text-3xl font-bold text-blue-400">{nodes.length}</div>
            <div className="text-sm text-gray-400 mt-1">Total Nodes</div>
          </div>
          <div className="bg-gray-900 border border-gray-700 rounded-xl p-4 text-center">
            <div className="text-3xl font-bold text-green-400">{edges.length}</div>
            <div className="text-sm text-gray-400 mt-1">Total Edges</div>
          </div>
        </div>

        {/* Tabs */}
        <div className="flex gap-2">
          {['nodes', 'edges'].map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`px-5 py-2 rounded-lg text-sm font-medium transition-colors ${
                tab === t ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700'
              }`}>
              {t === 'nodes' ? `Nodes (${nodes.length})` : `Edges (${edges.length})`}
            </button>
          ))}
        </div>

        {/* ── NODES TAB ── */}
        {tab === 'nodes' && (
          <div className="space-y-4">
            {/* Add node form */}
            <div className="bg-gray-900 border border-gray-700 rounded-2xl p-5">
              <h2 className="text-base font-semibold text-white mb-4">Add New Node</h2>
              <form onSubmit={handleAddNode} className="grid grid-cols-2 md:grid-cols-4 gap-3">
                <input required placeholder="Node name" value={nodeName}
                  onChange={e => setNodeName(e.target.value)}
                  className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm placeholder-gray-500 focus:outline-none focus:border-blue-500 col-span-2 md:col-span-1"
                />
                <select value={nodeType} onChange={e => setNodeType(e.target.value)}
                  className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-blue-500">
                  {NODE_TYPES.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                </select>
                <input required type="number" min="0" max="100" placeholder="X (0-100)" value={nodeX}
                  onChange={e => setNodeX(e.target.value)}
                  className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
                <input required type="number" min="0" max="100" placeholder="Y (0-100)" value={nodeY}
                  onChange={e => setNodeY(e.target.value)}
                  className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
                <button type="submit"
                  className="bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold px-4 py-2 rounded-lg transition-colors col-span-2 md:col-span-4">
                  Add Node
                </button>
              </form>
            </div>

            {/* Nodes table */}
            <div className="bg-gray-900 border border-gray-700 rounded-2xl overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-gray-800">
                  <tr>
                    {['ID', 'Name', 'Type', 'X', 'Y', 'Building', 'Action'].map(h => (
                      <th key={h} className="text-left px-4 py-3 text-gray-400 font-medium">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800">
                  {nodes.map(n => (
                    <tr key={n.id} className="hover:bg-gray-800/50">
                      <td className="px-4 py-3 text-gray-500">{n.id}</td>
                      <td className="px-4 py-3 text-white font-medium">{n.name}</td>
                      <td className="px-4 py-3 text-blue-400 text-xs">{n.nodeType?.replace(/_/g, ' ')}</td>
                      <td className="px-4 py-3 text-gray-400">{n.coordinateX}</td>
                      <td className="px-4 py-3 text-gray-400">{n.coordinateY}</td>
                      <td className="px-4 py-3 text-gray-400">{n.buildingName || '-'}</td>
                      <td className="px-4 py-3">
                        <Btn onClick={() => handleDelNode(n.id, n.name)} color="red">Delete</Btn>
                      </td>
                    </tr>
                  ))}
                  {nodes.length === 0 && (
                    <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-500">No nodes yet</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ── EDGES TAB ── */}
        {tab === 'edges' && (
          <div className="space-y-4">
            {/* Add edge form */}
            <div className="bg-gray-900 border border-gray-700 rounded-2xl p-5">
              <h2 className="text-base font-semibold text-white mb-4">Add New Edge (Path)</h2>
              <form onSubmit={handleAddEdge} className="grid grid-cols-1 md:grid-cols-4 gap-3">
                <select required value={srcId} onChange={e => setSrcId(e.target.value)}
                  className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-blue-500">
                  <option value="">From node...</option>
                  {nodes.map(n => <option key={n.id} value={n.id}>[{n.id}] {n.name}</option>)}
                </select>
                <select required value={tgtId} onChange={e => setTgtId(e.target.value)}
                  className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-blue-500">
                  <option value="">To node...</option>
                  {nodes.map(n => <option key={n.id} value={n.id}>[{n.id}] {n.name}</option>)}
                </select>
                <input required type="number" min="1" placeholder="Distance (metres)" value={dist}
                  onChange={e => setDist(e.target.value)}
                  className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-2 text-white text-sm placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
                <button type="submit"
                  className="bg-green-700 hover:bg-green-600 text-white text-sm font-semibold px-4 py-2 rounded-lg transition-colors">
                  Add Edge
                </button>
              </form>
            </div>

            {/* Edges table */}
            <div className="bg-gray-900 border border-gray-700 rounded-2xl overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-gray-800">
                  <tr>
                    {['ID', 'From', 'To', 'Distance', 'Bidirectional', 'Action'].map(h => (
                      <th key={h} className="text-left px-4 py-3 text-gray-400 font-medium">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800">
                  {edges.map(e => (
                    <tr key={e.id} className="hover:bg-gray-800/50">
                      <td className="px-4 py-3 text-gray-500">{e.id}</td>
                      <td className="px-4 py-3 text-white">{e.sourceName}</td>
                      <td className="px-4 py-3 text-white">{e.targetName}</td>
                      <td className="px-4 py-3 text-green-400 font-medium">{e.distanceMeters}m</td>
                      <td className="px-4 py-3">
                        <span className={`text-xs px-2 py-0.5 rounded-full ${e.bidirectional ? 'bg-blue-900/40 text-blue-300' : 'bg-gray-700 text-gray-400'}`}>
                          {e.bidirectional ? 'Yes' : 'One-way'}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <Btn onClick={() => handleDelEdge(e.id)} color="red">Delete</Btn>
                      </td>
                    </tr>
                  ))}
                  {edges.length === 0 && (
                    <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-500">No edges yet</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
