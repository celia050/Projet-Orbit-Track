import { useEffect, useState, type FormEvent } from 'react'
import { ApiError } from '../auth/api'
import {
  createAlert,
  deleteAlert,
  errorMessage,
  fetchAlerts,
  fetchEventTypes,
  setAlertActive,
  typeLabel,
  type Alert,
  type AlertForm,
} from './api'

interface Fields {
  name: string
  eventType: string
  keyword: string
  dateFrom: string
  dateTo: string
  latitude: string
  longitude: string
  radiusKm: string
}

const EMPTY: Fields = {
  name: '',
  eventType: '',
  keyword: '',
  dateFrom: '',
  dateTo: '',
  latitude: '',
  longitude: '',
  radiusKm: '',
}

function text(value: string): string | null {
  return value.trim() === '' ? null : value.trim()
}

function number(value: string): number | null {
  const cleaned = value.trim().replace(',', '.')
  return cleaned === '' ? null : Number(cleaned)
}

function toForm(fields: Fields): AlertForm {
  return {
    name: fields.name.trim(),
    eventType: text(fields.eventType),
    keyword: text(fields.keyword),
    dateFrom: text(fields.dateFrom),
    dateTo: text(fields.dateTo),
    latitude: number(fields.latitude),
    longitude: number(fields.longitude),
    radiusKm: number(fields.radiusKm),
  }
}

function formatDay(iso: string): string {
  return new Date(`${iso}T12:00:00`).toLocaleDateString('fr-FR')
}

/** Résumé lisible des critères d'une alerte. */
function describe(alert: Alert): string {
  const parts: string[] = []
  if (alert.eventType) parts.push(typeLabel(alert.eventType))
  if (alert.keyword) parts.push(`mot-clé « ${alert.keyword} »`)
  if (alert.dateFrom && alert.dateTo) {
    parts.push(`du ${formatDay(alert.dateFrom)} au ${formatDay(alert.dateTo)}`)
  } else if (alert.dateFrom) {
    parts.push(`à partir du ${formatDay(alert.dateFrom)}`)
  } else if (alert.dateTo) {
    parts.push(`jusqu’au ${formatDay(alert.dateTo)}`)
  }
  if (alert.radiusKm !== null) {
    parts.push(`à moins de ${alert.radiusKm} km de (${alert.latitude}, ${alert.longitude})`)
  }
  return parts.join(' · ')
}

/** Feature 11 : créer et gérer ses alertes. */
export function AlertsPage() {
  const [alerts, setAlerts] = useState<Alert[] | null>(null)
  const [types, setTypes] = useState<string[]>([])
  const [fields, setFields] = useState<Fields>(EMPTY)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)
  const [info, setInfo] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchAlerts()
      .then((list) => {
        if (!cancelled) setAlerts(list)
      })
      .catch((e: unknown) => {
        if (!cancelled) setError(errorMessage(e))
      })
    fetchEventTypes()
      .then((list) => {
        if (!cancelled) setTypes(list)
      })
      .catch(() => {
        /* la liste déroulante reste vide */
      })
    return () => {
      cancelled = true
    }
  }, [])

  function update(field: keyof Fields, value: string) {
    setFields((f) => ({ ...f, [field]: value }))
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setFieldErrors({})
    setInfo(null)
    try {
      const created = await createAlert(toForm(fields))
      setAlerts((list) => [created, ...(list ?? [])])
      setFields(EMPTY)
      setInfo('Alerte créée. Vous serez prévenu dans vos notifications.')
    } catch (e) {
      setError(errorMessage(e))
      if (e instanceof ApiError) setFieldErrors(e.fieldErrors)
    } finally {
      setSaving(false)
    }
  }

  async function toggle(alert: Alert) {
    setError(null)
    try {
      const updated = await setAlertActive(alert.id, !alert.active)
      setAlerts((list) => (list ?? []).map((a) => (a.id === updated.id ? updated : a)))
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  async function remove(alert: Alert) {
    if (!window.confirm(`Supprimer l’alerte « ${alert.name} » ?`)) return
    setError(null)
    try {
      await deleteAlert(alert.id)
      setAlerts((list) => (list ?? []).filter((a) => a.id !== alert.id))
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <section className="page">
      <h1>Mes alertes</h1>
      <p className="page-intro">
        Soyez prévenu dès qu’un phénomène correspond à vos critères. Choisissez au moins un critère ;
        un phénomène doit tous les remplir.
      </p>

      <form className="form-card" onSubmit={submit} noValidate>
        <label htmlFor="alert-name">Nom de l’alerte</label>
        <input
          id="alert-name"
          value={fields.name}
          maxLength={80}
          aria-invalid={fieldErrors.name ? true : undefined}
          onChange={(e) => update('name', e.target.value)}
          placeholder="Ex. : Étoiles filantes près de chez moi"
        />

        <div className="form-row">
          <div>
            <label htmlFor="alert-type">Type de phénomène</label>
            <select
              id="alert-type"
              value={fields.eventType}
              onChange={(e) => update('eventType', e.target.value)}
            >
              <option value="">Tous</option>
              {types.map((type) => (
                <option key={type} value={type}>
                  {typeLabel(type)}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="alert-keyword">Mot-clé</label>
            <input
              id="alert-keyword"
              value={fields.keyword}
              maxLength={80}
              onChange={(e) => update('keyword', e.target.value)}
              placeholder="Ex. : Géminides"
            />
          </div>
        </div>

        <div className="form-row">
          <div>
            <label htmlFor="alert-from">Entre le</label>
            <input
              id="alert-from"
              type="date"
              value={fields.dateFrom}
              onChange={(e) => update('dateFrom', e.target.value)}
            />
          </div>
          <div>
            <label htmlFor="alert-to">et le</label>
            <input
              id="alert-to"
              type="date"
              value={fields.dateTo}
              onChange={(e) => update('dateTo', e.target.value)}
            />
          </div>
        </div>

        <div className="form-row">
          <div>
            <label htmlFor="alert-lat">Latitude</label>
            <input
              id="alert-lat"
              inputMode="decimal"
              value={fields.latitude}
              aria-invalid={fieldErrors.latitude ? true : undefined}
              onChange={(e) => update('latitude', e.target.value)}
              placeholder="48.85"
            />
          </div>
          <div>
            <label htmlFor="alert-lon">Longitude</label>
            <input
              id="alert-lon"
              inputMode="decimal"
              value={fields.longitude}
              aria-invalid={fieldErrors.longitude ? true : undefined}
              onChange={(e) => update('longitude', e.target.value)}
              placeholder="2.35"
            />
          </div>
          <div>
            <label htmlFor="alert-radius">Rayon (km)</label>
            <input
              id="alert-radius"
              inputMode="decimal"
              value={fields.radiusKm}
              aria-invalid={fieldErrors.radiusKm ? true : undefined}
              onChange={(e) => update('radiusKm', e.target.value)}
              placeholder="50"
            />
          </div>
        </div>
        <p className="hint">Lieu facultatif : remplissez les trois champs ou aucun.</p>

        {error && (
          <p className="auth-error" role="alert">
            {error}
          </p>
        )}
        {info && <p role="status">{info}</p>}
        <div className="card-actions">
          <button type="submit" className="primary" disabled={saving}>
            {saving ? 'Création…' : 'Créer l’alerte'}
          </button>
        </div>
      </form>

      {alerts === null && !error && <p>Chargement…</p>}
      {alerts?.length === 0 && <p>Vous n’avez pas encore d’alerte.</p>}

      <ul className="card-list">
        {alerts?.map((alert) => (
          <li key={alert.id} className="card">
            <div className="card-head">
              <h2>{alert.name}</h2>
              <span className={alert.active ? 'tag tag-ok' : 'tag'}>
                {alert.active ? 'Active' : 'En pause'}
              </span>
            </div>
            <p className="card-meta">{describe(alert)}</p>
            <div className="card-actions">
              <button type="button" onClick={() => toggle(alert)}>
                {alert.active ? 'Mettre en pause' : 'Réactiver'}
              </button>
              <button type="button" onClick={() => remove(alert)}>
                Supprimer
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
