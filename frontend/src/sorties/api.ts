// Appels à l'API des sorties collectives (features 7 à 11 : sorties, forum, notifications, alertes).
import { ApiError, authFetch } from '../auth/api'

/** Appel à l'API avec le jeton de connexion. Gère les réponses vides (204) et les erreurs en français. */
export async function call<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response
  try {
    response = await authFetch(path, init)
  } catch {
    throw new ApiError('Le serveur ne répond pas. Réessayez dans un instant.', 0)
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(
      problem?.detail ?? `Erreur ${response.status}`,
      response.status,
      problem?.errors ?? {},
    )
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

/** Message d'erreur à afficher, quelle que soit l'erreur. */
export function errorMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : 'Une erreur inattendue est survenue.'
}

// ---------- Feature 7 : sorties d'observation ----------

export type EventStatus = 'SCHEDULED' | 'UPDATED' | 'CANCELLED'

export interface Outing {
  eventId: number
  name: string
  type: string
  startAt: string
  endAt: string
  status: EventStatus
  visibilityZone: string
  participantCount: number
  participating: boolean
}

export interface ParticipationState {
  eventId: number
  participantCount: number
  participating: boolean
}

export function fetchOutings(): Promise<Outing[]> {
  return call('/api/outings')
}

export function joinOuting(eventId: number): Promise<ParticipationState> {
  return call(`/api/events/${eventId}/participation`, { method: 'POST' })
}

export function leaveOuting(eventId: number): Promise<void> {
  return call(`/api/events/${eventId}/participation`, { method: 'DELETE' })
}

// ---------- Affichage ----------

const TYPE_LABELS: Record<string, string> = {
  METEOR_SHOWER: 'Pluie de météores',
  CONJUNCTION: 'Conjonction',
  ISS_PASS: 'Passage de l’ISS',
  LUNAR_ECLIPSE: 'Éclipse de Lune',
  SOLAR_ECLIPSE: 'Éclipse de Soleil',
  PLANETARY_ALIGNMENT: 'Alignement de planètes',
  PLANET_OBSERVATION: 'Observation de planète',
}

export function typeLabel(type: string): string {
  return TYPE_LABELS[type] ?? type
}

/** Les dates sont stockées en UTC : on les affiche à l'heure locale du navigateur. */
export function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('fr-FR', { dateStyle: 'full', timeStyle: 'short' })
}
