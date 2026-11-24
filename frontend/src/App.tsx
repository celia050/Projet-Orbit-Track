import { useEffect, useState } from 'react'
import { LoginForm, RegisterForm } from './auth/AuthForms'
import { useAuth } from './auth/useAuth'
import { ForumView } from './sorties/ForumView'
import { NotificationsPage } from './sorties/NotificationsPage'
import { OutingsPage } from './sorties/OutingsPage'
import { fetchUnreadCount, type Outing } from './sorties/api'
import './App.css'

type AuthMode = 'login' | 'register' | null
type Page = 'outings' | 'notifications'

/** Le nombre de notifications non lues est rafraîchi toutes les minutes. */
const UNREAD_REFRESH_MS = 60_000

function App() {
  const { user, loading, isAdmin, logout } = useAuth()
  const [mode, setMode] = useState<AuthMode>(null)
  const [page, setPage] = useState<Page>('outings')
  // Sortie dont le forum est ouvert (feature 8).
  const [forumOuting, setForumOuting] = useState<Outing | null>(null)
  const [unread, setUnread] = useState(0)
  const [unreadKey, setUnreadKey] = useState(0)

  useEffect(() => {
    if (!user) return
    let cancelled = false
    const refresh = () => {
      fetchUnreadCount()
        .then((result) => {
          if (!cancelled) setUnread(result.count)
        })
        .catch(() => {
          /* la pastille n'est pas indispensable */
        })
    }
    refresh()
    const timer = window.setInterval(refresh, UNREAD_REFRESH_MS)
    return () => {
      cancelled = true
      window.clearInterval(timer)
    }
  }, [user, unreadKey])

  function go(next: Page) {
    setPage(next)
    setForumOuting(null)
    setMode(null)
  }

  function handleLogout() {
    logout()
    go('outings')
  }

  const showPages = user !== null || mode === null

  return (
    <>
      <header className="topbar">
        <span className="brand">Orbit Track</span>
        {user && (
          <nav className="mainnav" aria-label="Menu principal">
            <button
              type="button"
              aria-current={page === 'outings' ? 'page' : undefined}
              onClick={() => go('outings')}
            >
              Sorties
            </button>
            <button
              type="button"
              aria-current={page === 'notifications' ? 'page' : undefined}
              onClick={() => go('notifications')}
            >
              Notifications
              {unread > 0 && <span className="counter">{unread}</span>}
            </button>
          </nav>
        )}
        <nav className="account" aria-label="Compte">
          {loading ? null : user ? (
            <>
              <span>
                Bonjour <strong>{user.displayName}</strong>
                {isAdmin && <span className="badge">Admin</span>}
              </span>
              <button type="button" onClick={handleLogout}>
                Se déconnecter
              </button>
            </>
          ) : (
            <>
              <button type="button" onClick={() => setMode('login')}>
                Se connecter
              </button>
              <button type="button" className="primary" onClick={() => setMode('register')}>
                Créer un compte
              </button>
            </>
          )}
        </nav>
      </header>

      <main className="content">
        {!user && mode === 'login' && (
          <LoginForm onSuccess={() => setMode(null)} onSwitch={() => setMode('register')} />
        )}
        {!user && mode === 'register' && (
          <RegisterForm onSuccess={() => setMode(null)} onSwitch={() => setMode('login')} />
        )}
        {/* Emplacement des pages des features (sorties, forum, alertes, carte, calendrier…). */}
        {showPages && user && page === 'notifications' && (
          <NotificationsPage onUnreadChange={() => setUnreadKey((k) => k + 1)} />
        )}
        {showPages && (!user || page === 'outings') &&
          (user && forumOuting ? (
            <ForumView outing={forumOuting} onBack={() => setForumOuting(null)} />
          ) : (
            <OutingsPage onLoginRequired={() => setMode('login')} onOpenForum={setForumOuting} />
          ))}
      </main>
    </>
  )
}

export default App
