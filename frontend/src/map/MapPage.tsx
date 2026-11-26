import { useRef, useState } from 'react'
import { toPng } from 'html-to-image'
import { EventMap } from './EventMap'
import {
  fetchRecommendedPlaces,
  type MapEvent,
  type ObservationPlace,
} from './api'
import { errorMessage } from '../sorties/api'
import './MapPage.css'

interface Props {
  events: MapEvent[]
}

export function MapPage({ events }: Props) {
  const exportRef = useRef<HTMLDivElement>(null)
  const requestId = useRef(0)

  const [recommendations, setRecommendations] = useState<{
    eventId: number
    places: ObservationPlace[]
  } | null>(null)

  const [loading, setLoading] = useState(false)
  const [exporting, setExporting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const selectedEvent = events.find(
    (event) =>
      event.id === recommendations?.eventId &&
      event.status !== 'CANCELLED',
  )

  const places = selectedEvent
    ? recommendations?.places ?? []
    : []

  const locatedEvents = events.filter(
    (event) =>
      event.latitude !== null &&
      event.longitude !== null,
  )

  const missingCoordinates =
    events.length - locatedEvents.length

  async function selectEvent(event: MapEvent) {
    if (
      event.latitude === null ||
      event.longitude === null
    ) {
      return
    }

    const currentRequest = ++requestId.current

    setRecommendations(null)
    setLoading(true)
    setError(null)

    try {
      const result = await fetchRecommendedPlaces(
        event.id,
        event.latitude,
        event.longitude,
      )

      if (currentRequest === requestId.current) {
        setRecommendations({
          eventId: event.id,
          places: result,
        })
      }
    } catch (e) {
      if (currentRequest === requestId.current) {
        setError(errorMessage(e))
      }
    } finally {
      if (currentRequest === requestId.current) {
        setLoading(false)
      }
    }
  }

  async function exportImage() {
    if (!exportRef.current) return

    setExporting(true)
    setError(null)

    try {
      const dataUrl = await toPng(exportRef.current, {
        backgroundColor: '#ffffff',
        pixelRatio: 2,
      })

      const link = document.createElement('a')
      link.download = 'orbit-track-carte.png'
      link.href = dataUrl
      link.click()
    } catch {
      setError(
        "Impossible d'exporter la carte. Réessayez ou utilisez l'impression.",
      )
    } finally {
      setExporting(false)
    }
  }

  return (
    <section className="map-page">
      <h1>Carte des phénomènes astronomiques</h1>

      <p className="page-intro">
        Explorez les phénomènes et découvrez les lieux
        d’observation conseillés.
      </p>

      <p>
        {events.length} phénomène(s), dont{' '}
        {locatedEvents.length} sur la carte.
      </p>

      {missingCoordinates > 0 && (
        <p className="hint">
          {missingCoordinates} phénomène(s) sans coordonnées
          géographiques restent consultables dans la liste.
        </p>
      )}

      <div className="map-actions">
        <button
          type="button"
          onClick={() => window.print()}
        >
          Imprimer la carte
        </button>

        <button
          type="button"
          className="primary"
          disabled={exporting}
          onClick={exportImage}
        >
          {exporting
            ? 'Export en cours…'
            : 'Enregistrer en PNG'}
        </button>
      </div>

      {error && (
        <p className="auth-error" role="alert">
          {error}
        </p>
      )}

      {loading && <p>Recherche des lieux conseillés…</p>}

      {selectedEvent && (
        <div className="card">
          <h2>Lieux conseillés : {selectedEvent.name}</h2>

          <p>{places.length} lieu(x) recommandé(s).</p>

          <p className="hint">
            Distances calculées depuis le centre de la
            zone du phénomène. La météo n’est pas garantie.
          </p>

          <button
            type="button"
            onClick={() => setRecommendations(null)}
          >
            Masquer les lieux conseillés
          </button>
        </div>
      )}

      <div className="map-export" ref={exportRef}>
        <h2>Orbit Track — Carte astronomique</h2>

        <EventMap
          events={locatedEvents}
          places={places}
          onSelectEvent={selectEvent}
        />

        <div className="map-legend">
          <span>
            <i
              className="map-dot"
              style={{ background: '#7c3aed' }}
            />
            Phénomène astronomique
          </span>

          <span>
            <i
              className="map-dot"
              style={{ background: '#16a34a' }}
            />
            Lieu conseillé
          </span>

          <span>
            <i
              className="map-dot"
              style={{ background: '#dc2626' }}
            />
            Phénomène annulé
          </span>

          <span>
            <i
              className="map-dot"
              style={{
                background: '#ede9fe',
                border: '1px solid #7c3aed',
              }}
            />
            Zone de visibilité
          </span>
        </div>

        <div className="map-source">
          <p>
            Date : {new Date().toLocaleDateString('fr-FR')}
          </p>
          <p>
            Fond de carte : © OpenStreetMap contributors
          </p>
          <p>
            https://www.openstreetmap.org/copyright
          </p>
        </div>
      </div>
    </section>
  )
}
