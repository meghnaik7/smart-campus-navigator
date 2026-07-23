import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import './index.css'
import LoginPage    from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import HomePage     from './pages/HomePage'
import AdminPage    from './pages/AdminPage'

// Simple auth guard — redirect to /login if no token
function Private({ children }) {
  return localStorage.getItem('token') ? children : <Navigate to="/login" replace />
}

// Admin guard — check role stored in localStorage
function AdminOnly({ children }) {
  const role = localStorage.getItem('role')
  if (!localStorage.getItem('token')) return <Navigate to="/login" replace />
  if (role !== 'ROLE_ADMIN')          return <Navigate to="/"      replace />
  return children
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Routes>
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/"         element={<Private><HomePage /></Private>} />
      <Route path="/admin"    element={<AdminOnly><AdminPage /></AdminOnly>} />
      <Route path="*"         element={<Navigate to="/" replace />} />
    </Routes>
  </BrowserRouter>
)
