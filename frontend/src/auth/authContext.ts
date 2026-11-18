import { createContext } from 'react'
import type { RegisterData, User } from './api'

export interface AuthContextValue {
  /** Utilisateur connecté, ou null si visiteur. */
  user: User | null
  /** true pendant la vérification du jeton au chargement de la page. */
  loading: boolean
  isAdmin: boolean
  login: (email: string, password: string) => Promise<void>
  register: (data: RegisterData) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
