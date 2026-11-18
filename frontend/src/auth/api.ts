// Appels à l'API d'authentification (feature 12) + outil authFetch pour les autres features.

export type Role = 'USER' | 'ADMIN'

export interface User {
  id: number
  email: string
  displayName: string
  role: Role
  createdAt: string
}

export interface AuthResponse {
  token: string
  expiresAt: string
  user: User
}

export interface RegisterData {
  email: string
  password: string
  displayName: string
}

/** Erreur renvoyée par l'API : `message` est déjà en français, prêt à afficher. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(message: string, status: number, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

const TOKEN_KEY = 'orbittrack.token'

/** Événement envoyé quand le jeton n'est plus valide (expiré, compte supprimé…). */
export const SESSION_EXPIRED_EVENT = 'orbittrack:session-expired'

// Le stockage du navigateur peut être indisponible (navigation privée stricte) : on ne plante pas.
export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    /* ignoré */
  }
}

export function clearToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    /* ignoré */
  }
}

/**
 * fetch qui ajoute automatiquement le jeton de connexion.
 * À utiliser par toutes les features pour les routes réservées aux membres connectés :
 *
 *   const res = await authFetch(`/api/events/${id}/participation`, { method: 'POST' })
 */
export async function authFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const token = getToken()
  const headers = new Headers(init.headers)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')

  const response = await fetch(path, { ...init, headers })
  if (response.status === 401 && token) {
    clearToken()
    window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT))
  }
  return response
}

/** `withToken: false` pour la connexion et l'inscription : un vieux jeton expiré ferait refuser la requête. */
async function request<T>(path: string, init: RequestInit = {}, withToken = true): Promise<T> {
  let response: Response
  try {
    response = withToken
      ? await authFetch(path, init)
      : await fetch(path, { ...init, headers: { 'Content-Type': 'application/json' } })
  } catch {
    throw new ApiError('Le serveur ne répond pas. Réessayez dans un instant.', 0)
  }
  if (!response.ok) {
    // Le backend renvoie un « Problem Details » : { detail, errors? }
    const problem = await response.json().catch(() => null)
    throw new ApiError(
      problem?.detail ?? `Erreur ${response.status}`,
      response.status,
      problem?.errors ?? {},
    )
  }
  return (await response.json()) as T
}

export function apiRegister(data: RegisterData): Promise<AuthResponse> {
  return request('/api/auth/register', { method: 'POST', body: JSON.stringify(data) }, false)
}

export function apiLogin(email: string, password: string): Promise<AuthResponse> {
  return request(
    '/api/auth/login',
    { method: 'POST', body: JSON.stringify({ email, password }) },
    false,
  )
}

export function apiMe(): Promise<User> {
  return request('/api/auth/me')
}
