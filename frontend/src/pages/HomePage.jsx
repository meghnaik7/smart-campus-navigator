import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getNodes, getEdges, getRoute, suggest } from '../api'
import CampusGraph from '../components/CampusGraph'

export default function HomePage() {
  const nav  = useNavigate()
  const name = localStorage.getItem('name') || 'User'
  const role = localStorage.getItem('role')
  const isAdmin = role === 'ROLE_ADMIN'

  const [nodes,    setNodes]    = useState([])
  const [edges,    setEdges]    = useState([])
  const [fromId,   setFromId]   = useState('')
  const [toId,     setToId]     = useState('')
  const [route,    setRoute]    = useState(null)
  const [error,    setError]    = useState('')
  const [loading,  setLoading]  = useState(false)
  const [suggestions, setSugg]  = useState([])
  const [searchQ,  setSearchQ]  = useState('')

  // Load nodes and edges on mount
  useEffect(() => {
    getNodes().then(setNodes).catch(() => setError('Could not load campus map'))
    getEdges().then(setEdges).catch(() => {})
  }, [])

  function logout() {
    localStorage.clear()
    nav('/login')
  }

  async function findRoute(e) {
    e.preventDefault()
    if (!fromId || !toId)        { setError('Please select both source and destination'); return }
    if (fromId === toId)         { setError('Source and destination cannot be the same');  return }
    setError(''); setRoute(null); setLoading(true)
    try {
      const data = await getRoute(fromId, toId)
      if (!data.found) { setError(data.message || 'No path found between these locations'); return }
      setRoute(data)
    } catch (err) {
      setError(err.message)
    } finally { setLoading(false) }
  }

  async function handleSearch(q) {
    setSearchQ(q)
    if (q.length < 2) { setSugg([]); return }
    try {
      const data = await suggest(q)
      setSugg(data || [])
    } catch { setSugg([]) }
  }

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">

      {/* Navbar */}
      <nav className="bg-gray-900 border-b border-gray-700 px-6 py-3 flex items-center justify-between">
        <span className="font-bold text-white text-lg">Smart Campus Navigator</span>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-400">
            Hello, <span className="text-white font-medium">{name}</span>
            {isAdmin && <span className="ml-2 text-xs bg-amber-500/20 text-amber-400 border border-amber-500/30 px-2 py-0.5 rounded-full">Admin</span>}
          </span>
          {isAdmin && (
            <button onClick={() => nav('/admin')}
              className="text-sm bg-gray-700 hover:bg-gray-600 px-3 py-1.5 rounded-lg transition-colors">
              Admin Panel
            </button>
          )}
          <button onClick={logout}
            className="text-sm bg-red-900/40 hover:bg-red-900/70 text-red-300 px-3 py-1.5 rounded-lg transition-colors">
            Logout
          </button>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto px-4 py-6 space-y-6">

        {/* Route finder card */}
        <div className="bg-gray-900 border border-gray-700 rounded-2xl p-6">
          <h2 className="text-lg font-semibold text-white mb-4">Find Shortest Path</h2>

          {/* Search box */}
          <div className="relative mb-4">
            <input
              value={searchQ}
              onChange={e => handleSearch(e.target.value)}
              placeholder="Search a location (e.g. Library, CSE Block)..."
              className="w-full bg-gray-800 border border-gray-600 rounded-lg px-4 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
            />
            {suggestions.length > 0 && (
              <div className="absolute top-full left-0 right-0 z-20 bg-gray-800 border border-gray-600 rounded-xl mt-1 shadow-xl overflow-hidden">
                {suggestions.map((s, i) => (
                  <div key={i}
                    onClick={() => { setSearchQ(s.displayText); setSugg([]) }}
                    className="px-4 py-2.5 hover:bg-gray-700 cursor-pointer flex items-center justify-between">
                    <span className="text-sm text-white">{s.displayText}</span>
                    <span className="text-xs text-gray-400">{s.entityType}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Source / Destination selects */}
          <form onSubmit={findRoute}>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
              <div>
                <label className="block text-sm text-gray-400 mb-1">From (Source)</label>
                <select value={fromId} onChange={e => setFromId(e.target.value)}
                  className="w-full bg-gray-800 border border-gray-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-blue-500">
                  <option value="">Select starting point...</option>
                  {nodes.map(n => (
                    <option key={n.id} value={n.id}>
                      {n.name}{n.buildingName ? ` (${n.buildingName})` : ''}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">To (Destination)</label>
                <select value={toId} onChange={e => setToId(e.target.value)}
                  className="w-full bg-gray-800 border border-gray-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-blue-500">
                  <option value="">Select destination...</option>
                  {nodes.map(n => (
                    <option key={n.id} value={n.id}>
                      {n.name}{n.buildingName ? ` (${n.buildingName})` : ''}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {error && (
              <div className="bg-red-900/40 border border-red-500/50 text-red-300 text-sm rounded-lg p-3 mb-4">
                {error}
              </div>
            )}

            <button type="submit" disabled={loading}
              className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white font-semibold px-6 py-2.5 rounded-lg transition-colors">
              {loading ? 'Finding route...' : 'Find Shortest Path'}
            </button>
          </form>
        </div>

        {/* Route result */}
        {route && (
          <div className="bg-gray-900 border border-gray-700 rounded-2xl p-6 space-y-4">
            <h2 className="text-lg font-semibold text-white">Route Result</h2>

            {/* Summary stats */}
            <div className="grid grid-cols-3 gap-3">
              {[
                ['Total Distance', route.distanceDisplay],
                ['Walking Time',   route.timeDisplay],
                ['Waypoints',      `${route.nodeCount} stops`],
              ].map(([label, val]) => (
                <div key={label} className="bg-gray-800 rounded-xl p-4 text-center">
                  <div className="text-xl font-bold text-blue-400">{val}</div>
                  <div className="text-xs text-gray-400 mt-1">{label}</div>
                </div>
              ))}
            </div>

            {/* Step by step path */}
            <div>
              <h3 className="text-sm font-medium text-gray-300 mb-2">Step-by-step directions</h3>
              <ol className="space-y-1">
                {route.path.map((step, i) => (
                  <li key={step.id} className="flex items-center gap-3">
                    <span className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0
                      ${i === 0                    ? 'bg-green-600 text-white'
                      : i === route.path.length - 1 ? 'bg-red-600 text-white'
                      : 'bg-gray-700 text-gray-300'}`}>
                      {i + 1}
                    </span>
                    <span className="text-sm text-gray-200">{step.name}</span>
                    {i === 0                     && <span className="text-xs text-green-400 font-semibold">START</span>}
                    {i === route.path.length - 1 && <span className="text-xs text-red-400 font-semibold">END</span>}
                  </li>
                ))}
              </ol>
            </div>
          </div>
        )}

        {/* SVG Graph */}
        <CampusGraph
          nodes={nodes}
          edges={edges}
          routePath={route?.path || []}
        />

      </div>
    </div>
  )
}
