import {
  MapContainer,
  TileLayer,
  Circle,
  CircleMarker,
  Popup,
} from 'react-leaflet'
import { formatDate, typeLabel } from '../sorties/api'
import type { MapEvent, ObservationPlace } from './api'
import 'leaflet/dist/leaflet.css'

interface Props {
  events: MapEvent[]
  places: ObservationPlace[]
  onSelectEvent: (event: MapEvent) => void
}

export function EventMap({
  events,
  places,
  onSelectEvent,
}: Props) {
  return (
    <MapContainer
      center={[46.6034, 1.8883]}
      zoom={5}
      scrollWheelZoom
      className="orbit-map"
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        crossOrigin="anonymous"
        maxZoom={19}
      />

      {events.map((event) => {
        if (
          event.latitude === null ||
          event.longitude === null
        ) {
          return null
        }

        const position: [number, number] = [
          event.latitude,
          event.longitude,
        ]

        const color =
          event.status === 'CANCELLED'
            ? '#dc2626'
            : '#7c3aed'

        return (
          <div key={event.id}>
            {event.visibilityRadiusKm !== null &&
              event.visibilityRadiusKm > 0 && (
                <Circle
                  center={position}
                  radius={event.visibilityRadiusKm * 1000}
                  pathOptions={{
                    color,
                    fillColor: color,
                    fillOpacity: 0.08,
                    weight: 1,
                  }}
                  interactive={false}
                />
              )}

            <CircleMarker
              center={position}
              radius={9}
              pathOptions={{
                color: '#ffffff',
                fillColor: color,
                fillOpacity: 1,
                weight: 2,
              }}
            >
              <Popup>
                <strong>{event.name}</strong>
                <p>{typeLabel(event.type)}</p>
                <p>{formatDate(event.startAt)}</p>
                <p>{event.visibilityZone}</p>

                {event.status === 'CANCELLED' ? (
                  <p>Phénomène annulé</p>
                ) : (
                  <button
                    type="button"
                    onClick={() => onSelectEvent(event)}
                  >
                    Voir les lieux conseillés
                  </button>
                )}
              </Popup>
            </CircleMarker>
          </div>
        )
      })}

      {places.map((place) => (
        <CircleMarker
          key={place.id}
          center={[place.latitude, place.longitude]}
          radius={8}
          pathOptions={{
            color: '#ffffff',
            fillColor: '#16a34a',
            fillOpacity: 1,
            weight: 2,
          }}
        >
          <Popup>
            <strong>{place.name}</strong>
            <p>{place.description}</p>
            <p>
              Pollution lumineuse : {place.lightPollutionLevel}
            </p>
            <p>
              Accessibilité : {place.accessibilityScore}
            </p>
            {place.distanceKm !== null && (
              <p>Distance : {place.distanceKm} km</p>
            )}
            {place.score !== null && (
              <p>Score : {place.score}</p>
            )}
          </Popup>
        </CircleMarker>
      ))}
    </MapContainer>
  )
}
