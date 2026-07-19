import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Navbar       from './components/Navbar'
import LoginPage    from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import MapPage      from './pages/MapPage'
import SearchPage   from './pages/SearchPage'
import FacultyPage  from './pages/FacultyPage'
import ClassroomsPage from './pages/ClassroomsPage'
import EventsPage   from './pages/EventsPage'
import AdminPage    from './pages/AdminPage'

function PrivateRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <i className="fas fa-compass fa-spin text-4xl text-blue-400 mb-3 block"></i>
        <div className="text-slate-400">Loading Smart Campus Navigator...</div>
      </div>
    </div>
  )
  return user ? children : <Navigate to="/login" replace />
}

function AdminRoute({ children }) {
  const { user, isAdmin, loading } = useAuth()
  if (loading) return null
  if (!user) return <Navigate to="/login" replace />
  if (!isAdmin()) return <Navigate to="/dashboard" replace />
  return children
}

function AppRoutes() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1">
        <Routes>
          <Route path="/"         element={<Navigate to="/dashboard" replace />} />
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/dashboard"  element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
          <Route path="/map"        element={<PrivateRoute><MapPage /></PrivateRoute>} />
          <Route path="/search"     element={<PrivateRoute><SearchPage /></PrivateRoute>} />
          <Route path="/faculty"    element={<PrivateRoute><FacultyPage /></PrivateRoute>} />
          <Route path="/classrooms" element={<PrivateRoute><ClassroomsPage /></PrivateRoute>} />
          <Route path="/events"     element={<PrivateRoute><EventsPage /></PrivateRoute>} />
          <Route path="/admin"      element={<AdminRoute><AdminPage /></AdminRoute>} />
          <Route path="*" element={
            <div className="min-h-screen flex items-center justify-center">
              <div className="text-center">
                <div className="text-8xl font-black text-white/10 mb-4">404</div>
                <div className="text-2xl font-bold text-white mb-2">Page Not Found</div>
                <a href="/dashboard" className="btn-primary mt-4 inline-block">Go to Dashboard</a>
              </div>
            </div>
          }/>
        </Routes>
      </main>
    </div>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}
