import { useEffect, useState } from 'react'
import { useAuth } from '../auth/useAuth'
import {
  errorMessage,
  fetchOutings,
  formatDate,
  joinOuting,
  leaveOuting,
  typeLabel,
  type Outing,
} from './api'

interface OutingsPageProps {
  /** Demande à se connecter (le visiteur clique sur « S'inscrire »). */
  onLoginRequired: () => void
  /** Ouvre le forum d'une sortie (feature 8). Absent tant que le forum n'existe pas. */
  onOpenForum?: (outing: Outing) => void
}

/** Feature 7 : liste des sorties d'observation, inscription et désinscription. */
export function OutingsPage({ onLoginRequired, onOpenForum }: OutingsPageProps) {
  const { user, isAdmin } = useAuth()
  const [outings, setOutings] = useState<Outing[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  // Recharge quand on se connecte ou se déconnecte (le champ « participating » change).
  useEffect(() => {
    let cancelled = false
    fetchOutings()
      .then((list) => {
        if (!cancelled) {
          setOutings(list)
          setError(null)
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) setError(errorMessage(e))
      })
    return () => {
      cancelled = true
    }
  }, [user])

  async function toggle(outing: Outing) {
    if (!user) {
      onLoginRequired()
      return
    }
    setBusyId(outing.eventId)
    setError(null)
    try {
      const state = outing.participating
        ? await leaveOuting(outing.eventId).then(() => ({
            participating: false,
            participantCount: outing.participantCount - 1,
          }))
        : await joinOuting(outing.eventId)
      setOutings((list) =>
        (list ?? []).map((o) =>
          o.eventId === outing.eventId
            ? { ...o, participating: state.participating, participantCount: state.participantCount }
            : o,
        ),
      )
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <section className="page">
      <h1>Sorties d’observation</h1>
      <p className="page-intro">
        Inscrivez-vous pour observer un phénomène avec d’autres passionnés.
        {!user && ' La liste est libre d’accès ; il faut un compte pour s’inscrire.'}
      </p>

      {error && (
        <p className="auth-error" role="alert">
          {error}
        </p>
      )}
      {outings === null && !error && <p>Chargement…</p>}
      {outings?.length === 0 && <p>Aucune sortie à venir pour le moment.</p>}

      <ul className="card-list">
        {outings?.map((outing) => {
          const cancelled = outing.status === 'CANCELLED'
          return (
            <li key={outing.eventId} className="card">
              <div className="card-head">
                <h2>{outing.name}</h2>
                {cancelled && <span className="tag tag-danger">Annulé</span>}
                {outing.status === 'UPDATED' && <span className="tag">Horaire modifié</span>}
                {outing.participating && <span className="tag tag-ok">Inscrit</span>}
              </div>
              <p className="card-meta">
                {typeLabel(outing.type)} · {formatDate(outing.startAt)} · {outing.visibilityZone}
              </p>
              <p className="card-meta">
                {outing.participantCount} participant{outing.participantCount > 1 ? 's' : ''}
              </p>
              <div className="card-actions">
                {(!cancelled || outing.participating) && (
                  <button
                    type="button"
                    className={outing.participating ? undefined : 'primary'}
                    disabled={busyId === outing.eventId}
                    onClick={() => toggle(outing)}
                  >
                    {outing.participating ? 'Se désinscrire' : 'S’inscrire'}
                  </button>
                )}
                {onOpenForum && (outing.participating || isAdmin) && (
                  <button type="button" onClick={() => onOpenForum(outing)}>
                    Forum
                  </button>
                )}
              </div>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
