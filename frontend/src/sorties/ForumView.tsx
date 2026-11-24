import { useEffect, useState, type FormEvent } from 'react'
import {
  deleteForumMessage,
  editForumMessage,
  errorMessage,
  fetchForum,
  formatDate,
  postForumMessage,
  type ForumMessage,
  type ForumPage,
  type Outing,
} from './api'

const MAX_LENGTH = 1000

interface ForumViewProps {
  outing: Outing
  onBack: () => void
}

/** Feature 8 : forum d'une sortie (réservé aux inscrits et aux administrateurs). */
export function ForumView({ outing, onBack }: ForumViewProps) {
  const [page, setPage] = useState(0)
  // Incrémenté pour recharger la page courante après une action.
  const [reloadKey, setReloadKey] = useState(0)
  const [data, setData] = useState<ForumPage | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [draft, setDraft] = useState('')
  const [sending, setSending] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editText, setEditText] = useState('')

  useEffect(() => {
    let cancelled = false
    fetchForum(outing.eventId, page)
      .then((result) => {
        if (!cancelled) {
          setData(result)
          setError(null)
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) setError(errorMessage(e))
      })
    return () => {
      cancelled = true
    }
  }, [outing.eventId, page, reloadKey])

  function reload(toFirstPage = false) {
    if (toFirstPage) setPage(0)
    setReloadKey((k) => k + 1)
  }

  async function send(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSending(true)
    setError(null)
    try {
      await postForumMessage(outing.eventId, draft)
      setDraft('')
      reload(true)
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setSending(false)
    }
  }

  async function saveEdit(message: ForumMessage) {
    setError(null)
    try {
      await editForumMessage(message.id, editText)
      setEditingId(null)
      reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  async function remove(message: ForumMessage) {
    if (!window.confirm('Supprimer ce message ?')) return
    setError(null)
    try {
      await deleteForumMessage(message.id)
      reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <section className="page">
      <button type="button" className="link" onClick={onBack}>
        ← Retour aux sorties
      </button>
      <h1>Forum : {outing.name}</h1>
      <p className="page-intro">{formatDate(outing.startAt)} · réservé aux participants inscrits</p>

      <form className="form-card" onSubmit={send}>
        <label htmlFor="forum-draft">Votre message</label>
        <textarea
          id="forum-draft"
          rows={3}
          maxLength={MAX_LENGTH}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
        />
        <p className="hint">
          {draft.length} / {MAX_LENGTH} caractères
        </p>
        <div className="card-actions">
          <button type="submit" className="primary" disabled={sending || draft.trim() === ''}>
            {sending ? 'Envoi…' : 'Publier'}
          </button>
        </div>
      </form>

      {error && (
        <p className="auth-error" role="alert">
          {error}
        </p>
      )}
      {data === null && !error && <p>Chargement…</p>}
      {data?.totalItems === 0 && <p>Aucun message pour l’instant. Lancez la discussion !</p>}

      <ul className="card-list">
        {data?.items.map((message) => (
          <li key={message.id} className="card">
            <p className="card-meta">
              <strong>{message.authorName}</strong> · {formatDate(message.createdAt)}
              {message.edited && !message.deleted && ' · modifié'}
            </p>
            {editingId === message.id ? (
              <div className="form-card">
                <textarea
                  aria-label="Modifier le message"
                  rows={3}
                  maxLength={MAX_LENGTH}
                  value={editText}
                  onChange={(e) => setEditText(e.target.value)}
                />
                <div className="card-actions">
                  <button
                    type="button"
                    className="primary"
                    disabled={editText.trim() === ''}
                    onClick={() => saveEdit(message)}
                  >
                    Enregistrer
                  </button>
                  <button type="button" onClick={() => setEditingId(null)}>
                    Annuler
                  </button>
                </div>
              </div>
            ) : (
              <p className={message.deleted ? 'card-meta' : 'card-text'}>
                {message.deleted ? <em>{message.content}</em> : message.content}
              </p>
            )}
            {editingId !== message.id && (message.canEdit || message.canDelete) && (
              <div className="card-actions">
                {message.canEdit && (
                  <button
                    type="button"
                    onClick={() => {
                      setEditingId(message.id)
                      setEditText(message.content)
                    }}
                  >
                    Modifier
                  </button>
                )}
                {message.canDelete && (
                  <button type="button" onClick={() => remove(message)}>
                    Supprimer
                  </button>
                )}
              </div>
            )}
          </li>
        ))}
      </ul>

      {data && data.totalPages > 1 && (
        <nav className="pager" aria-label="Pages du forum">
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Plus récents
          </button>
          <span>
            Page {page + 1} / {data.totalPages}
          </span>
          <button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>
            Plus anciens
          </button>
        </nav>
      )}
    </section>
  )
}
