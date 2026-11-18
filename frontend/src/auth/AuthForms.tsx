import { useState, type FormEvent } from 'react'
import { ApiError } from './api'
import { useAuth } from './useAuth'

interface FormProps {
  /** Appelé quand la connexion ou l'inscription a réussi. */
  onSuccess: () => void
  /** Passer de « connexion » à « inscription » et inversement. */
  onSwitch: () => void
}

function messageOf(error: unknown): { message: string; fields: Record<string, string> } {
  if (error instanceof ApiError) return { message: error.message, fields: error.fieldErrors }
  return { message: 'Une erreur inattendue est survenue.', fields: {} }
}

export function LoginForm({ onSuccess, onSwitch }: FormProps) {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await login(email, password)
      onSuccess()
    } catch (e) {
      setError(messageOf(e).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="auth-card" onSubmit={handleSubmit} noValidate>
      <h2>Se connecter</h2>

      <label htmlFor="login-email">Email</label>
      <input
        id="login-email"
        type="email"
        autoComplete="email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />

      <label htmlFor="login-password">Mot de passe</label>
      <input
        id="login-password"
        type="password"
        autoComplete="current-password"
        required
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />

      {error && (
        <p className="auth-error" role="alert">
          {error}
        </p>
      )}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Connexion…' : 'Se connecter'}
      </button>
      <p className="auth-switch">
        Pas encore de compte ?{' '}
        <button type="button" className="link" onClick={onSwitch}>
          Créer un compte
        </button>
      </p>
    </form>
  )
}

export function RegisterForm({ onSuccess, onSwitch }: FormProps) {
  const { register } = useAuth()
  const [email, setEmail] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setFieldErrors({})
    try {
      await register({ email, displayName, password })
      onSuccess()
    } catch (e) {
      const { message, fields } = messageOf(e)
      setFieldErrors(fields)
      if (Object.keys(fields).length === 0) setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="auth-card" onSubmit={handleSubmit} noValidate>
      <h2>Créer un compte</h2>

      <label htmlFor="register-email">Email</label>
      <input
        id="register-email"
        type="email"
        autoComplete="email"
        required
        value={email}
        aria-invalid={Boolean(fieldErrors.email)}
        aria-describedby={fieldErrors.email ? 'register-email-error' : undefined}
        onChange={(e) => setEmail(e.target.value)}
      />
      {fieldErrors.email && (
        <p id="register-email-error" className="auth-error">
          {fieldErrors.email}
        </p>
      )}

      <label htmlFor="register-name">Pseudo</label>
      <input
        id="register-name"
        type="text"
        autoComplete="nickname"
        required
        value={displayName}
        aria-invalid={Boolean(fieldErrors.displayName)}
        aria-describedby={fieldErrors.displayName ? 'register-name-error' : undefined}
        onChange={(e) => setDisplayName(e.target.value)}
      />
      {fieldErrors.displayName && (
        <p id="register-name-error" className="auth-error">
          {fieldErrors.displayName}
        </p>
      )}

      <label htmlFor="register-password">Mot de passe (8 caractères minimum)</label>
      <input
        id="register-password"
        type="password"
        autoComplete="new-password"
        required
        minLength={8}
        value={password}
        aria-invalid={Boolean(fieldErrors.password)}
        aria-describedby={fieldErrors.password ? 'register-password-error' : undefined}
        onChange={(e) => setPassword(e.target.value)}
      />
      {fieldErrors.password && (
        <p id="register-password-error" className="auth-error">
          {fieldErrors.password}
        </p>
      )}

      {error && (
        <p className="auth-error" role="alert">
          {error}
        </p>
      )}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Création…' : 'Créer mon compte'}
      </button>
      <p className="auth-switch">
        Déjà inscrit ?{' '}
        <button type="button" className="link" onClick={onSwitch}>
          Se connecter
        </button>
      </p>
    </form>
  )
}
