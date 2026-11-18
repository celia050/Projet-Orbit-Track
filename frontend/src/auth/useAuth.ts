import { useContext } from 'react'
import { AuthContext, type AuthContextValue } from './authContext'

/**
 * Accès à l'utilisateur connecté depuis n'importe quel composant :
 *
 *   const { user, isAdmin } = useAuth()
 */
export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (value === null) {
    throw new Error('useAuth doit être utilisé à l’intérieur de <AuthProvider>.')
  }
  return value
}
