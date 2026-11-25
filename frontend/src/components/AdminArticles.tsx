import { useCallback, useEffect, useState } from 'react'
import { authFetch } from '../auth/api'

type Article = {
  id: number
  title: string
  content: string
  articleDate: string
  status: string
}

function AdminArticles() {
  const [articles, setArticles] = useState<Article[]>([])
  const [selected, setSelected] = useState<Article | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadDrafts = useCallback(async () => {
    try {
      const response = await authFetch('/api/admin/articles/drafts')

      if (!response.ok) {
        throw new Error('Impossible de récupérer les brouillons.')
      }

      const data = (await response.json()) as Article[]
      setArticles(data)
      setError('')
    } catch {
      setError('Erreur lors du chargement des brouillons.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let cancelled = false

    authFetch('/api/admin/articles/drafts')
      .then(async (response) => {
        if (!response.ok) {
          throw new Error('Impossible de récupérer les brouillons.')
        }

        const data = (await response.json()) as Article[]

        if (!cancelled) {
          setArticles(data)
          setError('')
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError('Erreur lors du chargement des brouillons.')
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  async function saveDraft() {
    if (!selected || busy) return

    setBusy(true)
    setError('')
    setMessage('')

    try {
      const response = await authFetch(
        `/api/admin/articles/${selected.id}`,
        {
          method: 'PUT',
          body: JSON.stringify(selected),
        },
      )

      if (!response.ok) {
        throw new Error('Impossible de modifier cet article.')
      }

      setMessage('Brouillon enregistré.')
      await loadDrafts()
    } catch {
      setError('Erreur lors de la modification.')
    } finally {
      setBusy(false)
    }
  }

  async function changeStatus(action: 'publish' | 'reject') {
    if (!selected || busy) return

    setBusy(true)
    setError('')
    setMessage('')

    try {
      const response = await authFetch(
        `/api/admin/articles/${selected.id}/${action}`,
        { method: 'POST' },
      )

      if (!response.ok) {
        throw new Error('Impossible de modifier le statut.')
      }

      setSelected(null)
      setMessage(
        action === 'publish'
          ? 'Article publié avec succès.'
          : 'Article rejeté.',
      )
      await loadDrafts()
    } catch {
      setError('Erreur lors du changement de statut.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="admin-articles">
      <h2>Administration des articles</h2>

      {loading && <p>Chargement des brouillons...</p>}
      {error && <p role="alert">{error}</p>}
      {message && <p role="status">{message}</p>}

      {!loading && articles.length === 0 && (
        <p>Aucun article en attente de validation.</p>
      )}

      <h3>Brouillons en attente</h3>

      {articles.map((article) => (
        <div key={article.id}>
          <strong>{article.title}</strong>
          {' — '}
          <button
            type="button"
            disabled={busy}
            onClick={() => {
              setSelected({ ...article })
              setMessage('')
            }}
          >
            Modifier / Valider
          </button>
        </div>
      ))}

      {selected && (
        <div className="article-editor">
          <h3>Modifier l'article #{selected.id}</h3>

          <label htmlFor="article-title">Titre</label>
          <input
            id="article-title"
            value={selected.title}
            onChange={(event) =>
              setSelected({ ...selected, title: event.target.value })
            }
          />

          <label htmlFor="article-content">Contenu</label>
          <textarea
            id="article-content"
            rows={8}
            value={selected.content}
            onChange={(event) =>
              setSelected({ ...selected, content: event.target.value })
            }
          />

          <button type="button" disabled={busy} onClick={saveDraft}>
            Enregistrer
          </button>

          <button
            type="button"
            disabled={busy}
            onClick={() => changeStatus('publish')}
          >
            Publier
          </button>

          <button
            type="button"
            disabled={busy}
            onClick={() => changeStatus('reject')}
          >
            Rejeter
          </button>

          <button
            type="button"
            disabled={busy}
            onClick={() => setSelected(null)}
          >
            Annuler
          </button>
        </div>
      )}
    </section>
  )
}

export default AdminArticles