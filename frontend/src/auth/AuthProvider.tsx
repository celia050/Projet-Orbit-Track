import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import {
  apiLogin,
  apiMe,
  apiRegister,
  clearToken,
  getToken,
  setToken,
  SESSION_EXPIRED_EVENT,
  type RegisterData,
  type User,
} from './api'
import { AuthContext, type AuthContextValue } from './authContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  // S'il y a un jeton enregistré, on vérifie qu'il est encore valide avant d'afficher l'état connecté.
  const [loading, setLoading] = useState(() => getToken() !== null)

  useEffect(() => {
    if (getToken() === null) return
    let cancelled = false
    apiMe()
      .then((me) => {
        if (!cancelled) setUser(me)
      })
      .catch(() => {
        clearToken()
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  // Jeton expiré pendant la navigation : on repasse en visiteur.
  useEffect(() => {
    const onExpired = () => setUser(null)
    window.addEventListener(SESSION_EXPIRED_EVENT, onExpired)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, onExpired)
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await apiLogin(email, password)
    setToken(response.token)
    setUser(response.user)
  }, [])

  const register = useCallback(async (data: RegisterData) => {
    const response = await apiRegister(data)
    setToken(response.token)
    setUser(response.user)
  }, [])

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ user, loading, isAdmin: user?.role === 'ADMIN', login, register, logout }),
    [user, loading, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
