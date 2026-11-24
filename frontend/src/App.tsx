import { useState } from 'react'
import { LoginForm, RegisterForm } from './auth/AuthForms'
import { useAuth } from './auth/useAuth'
import { ForumView } from './sorties/ForumView'
import { OutingsPage } from './sorties/OutingsPage'
import type { Outing } from './sorties/api'
import './App.css'

type AuthMode = 'login' | 'register' | null

function App() {
  const { user, loading, isAdmin, logout } = useAuth()
  const [mode, setMode] = useState<AuthMode>(null)
  // Sortie dont le forum est ouvert (feature 8), sinon la liste des sorties est affichée.
  const [forumOuting, setForumOuting] = useState<Outing | null>(null)

  function handleLogout() {
    logout()
    setForumOuting(null)
  }

  return (
    <>
      <header className="topbar">
        <span className="brand">Orbit Track</span>
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
        {(user || mode === null) &&
          // Emplacement des pages des features (sorties, forum, alertes, carte, calendrier…).
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
