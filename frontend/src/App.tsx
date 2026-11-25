import { useState } from 'react'
import { LoginForm, RegisterForm } from './auth/AuthForms'
import { useAuth } from './auth/useAuth'
import ArticleDuJour from './components/ArticleDuJour'
import AdminArticles from './components/AdminArticles'
import './App.css'

type AuthMode = 'login' | 'register' | null

function App() {
  const { user, loading, isAdmin, logout } = useAuth()
  const [mode, setMode] = useState<AuthMode>(null)

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

              <button type="button" onClick={logout}>
                Se déconnecter
              </button>
            </>
          ) : (
            <>
              <button type="button" onClick={() => setMode('login')}>
                Se connecter
              </button>

              <button
                type="button"
                className="primary"
                onClick={() => setMode('register')}
              >
                Créer un compte
              </button>
            </>
          )}
        </nav>
      </header>

      <main className="content">
        {!user && mode === 'login' && (
          <LoginForm
            onSuccess={() => setMode(null)}
            onSwitch={() => setMode('register')}
          />
        )}

        {!user && mode === 'register' && (
          <RegisterForm
            onSuccess={() => setMode(null)}
            onSwitch={() => setMode('login')}
          />
        )}

        {(user || mode === null) && (
          <>
            {/* Emplacement des autres fonctionnalités */}
            <p className="placeholder">
              Les phénomènes à venir s’afficheront ici.
            </p>

            {/* Feature 15 : Article du jour */}
            <ArticleDuJour />

            {/* Gestion des articles réservée aux administrateurs */}
            {isAdmin && <AdminArticles />}
          </>
        )}
      </main>
    </>
  )
}

export default App
