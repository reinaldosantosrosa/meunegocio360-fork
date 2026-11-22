import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { logout } from '../api/authApi.js'
import { AuthContext } from './AuthContext.js'
import {
  ApiError,
  configureAuthLifecycle,
  refreshAuthSession,
} from '../../../shared/lib/http.js'

export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const location = useLocation()
  const [status, setStatus] = useState('loading')
  const [accessToken, setAccessToken] = useState(null)
  const [user, setUser] = useState(null)
  const accessTokenRef = useRef(null)

  const getAccessToken = useCallback(() => accessTokenRef.current, [])

  const clearSession = useCallback(() => {
    accessTokenRef.current = null
    setAccessToken(null)
    setUser(null)
  }, [])

  const acceptSession = useCallback((session) => {
    accessTokenRef.current = session.accessToken
    setAccessToken(session.accessToken)
    setUser(session.usuario)
    setStatus('authenticated')
  }, [])

  const handleExpiredSession = useCallback(() => {
    clearSession()
    setStatus('anonymous')
    queryClient.clear()
    const from = `${location.pathname}${location.search}${location.hash}`
    navigate('/login', {
      replace: true,
      state: { from, sessionExpired: true },
    })
  }, [
    clearSession,
    location.hash,
    location.pathname,
    location.search,
    navigate,
    queryClient,
  ])

  useEffect(() => {
    configureAuthLifecycle({
      getAccessToken,
      onSessionUpdated: acceptSession,
      onSessionExpired: handleExpiredSession,
    })
  }, [acceptSession, getAccessToken, handleExpiredSession])

  useEffect(() => {
    let active = true

    refreshAuthSession().catch((error) => {
      if (!active) {
        return
      }

      clearSession()
      if (error instanceof ApiError && error.status === 401) {
        setStatus('anonymous')
      } else {
        setStatus('unavailable')
      }
    })

    return () => {
      active = false
    }
  }, [clearSession])

  const retryRestore = useCallback(async () => {
    setStatus('loading')
    try {
      await refreshAuthSession()
    } catch (error) {
      clearSession()
      setStatus(
        error instanceof ApiError && error.status === 401
          ? 'anonymous'
          : 'unavailable',
      )
    }
  }, [clearSession])

  const signOut = useCallback(async () => {
    let failure = null

    try {
      await logout()
    } catch (error) {
      if (!(error instanceof ApiError && error.status === 0)) {
        failure = error
      }
    } finally {
      clearSession()
      setStatus('anonymous')
      queryClient.clear()
    }

    if (failure) {
      throw failure
    }
  }, [clearSession, queryClient])

  const value = useMemo(
    () => ({
      status,
      accessToken,
      user,
      acceptSession,
      retryRestore,
      signOut,
    }),
    [accessToken, acceptSession, retryRestore, signOut, status, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
