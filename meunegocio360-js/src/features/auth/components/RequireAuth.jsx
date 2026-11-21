import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../hooks/useAuth.js'

export function RequireAuth({ children }) {
  const { status } = useAuth()
  const location = useLocation()

  if (status !== 'authenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return children
}
