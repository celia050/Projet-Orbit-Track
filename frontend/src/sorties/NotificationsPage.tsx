import { useEffect, useState } from 'react'
import {
  errorMessage,
  fetchNotifications,
  formatDate,
  markAllNotificationsRead,
  markNotificationRead,
  type AppNotification,
} from './api'

const TYPE_TITLES: Record<AppNotification['type'], string> = {
  EVENT_CHANGED: 'Horaire modifié',
  EVENT_CANCELLED: 'Sortie annulée',
  ALERT_MATCH: 'Alerte',
}

interface NotificationsPageProps {
  /** Appelé quand le nombre de notifications non lues change. */
  onUnreadChange: () => void
}

/** Feature 10 : mes notifications (changement d'horaire, annulation, alertes). */
export function NotificationsPage({ onUnreadChange }: NotificationsPageProps) {
  const [items, setItems] = useState<AppNotification[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchNotifications()
      .then((list) => {
        if (!cancelled) setItems(list)
      })
      .catch((e: unknown) => {
        if (!cancelled) setError(errorMessage(e))
      })
    return () => {
      cancelled = true
    }
  }, [])

  async function read(notification: AppNotification) {
    try {
      const updated = await markNotificationRead(notification.id)
      setItems((list) => (list ?? []).map((n) => (n.id === updated.id ? updated : n)))
      onUnreadChange()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  async function readAll() {
    try {
      await markAllNotificationsRead()
      setItems((list) => (list ?? []).map((n) => ({ ...n, read: true })))
      onUnreadChange()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  const hasUnread = items?.some((n) => !n.read) ?? false

  return (
    <section className="page">
      <h1>Mes notifications</h1>
      <p className="page-intro">
        Vous êtes prévenu ici quand une sortie à laquelle vous êtes inscrit change d’horaire ou est
        annulée, et quand un phénomène correspond à l’une de vos alertes.
      </p>

      {hasUnread && (
        <div className="card-actions">
          <button type="button" onClick={readAll}>
            Tout marquer comme lu
          </button>
        </div>
      )}
      {error && (
        <p className="auth-error" role="alert">
          {error}
        </p>
      )}
      {items === null && !error && <p>Chargement…</p>}
      {items?.length === 0 && <p>Aucune notification pour le moment.</p>}

      <ul className="card-list">
        {items?.map((notification) => (
          <li key={notification.id} className={notification.read ? 'card' : 'card unread'}>
            <div className="card-head">
              <strong>{TYPE_TITLES[notification.type]}</strong>
              {!notification.read && <span className="tag">Nouveau</span>}
            </div>
            <p className="card-text">{notification.message}</p>
            <p className="card-meta">{formatDate(notification.createdAt)}</p>
            {!notification.read && (
              <div className="card-actions">
                <button type="button" onClick={() => read(notification)}>
                  Marquer comme lu
                </button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </section>
  )
}
