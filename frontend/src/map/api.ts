import { call } from '../sorties/api'

export interface MapEvent {
  id: number
  name: string
  type: string
  description: string
  startAt: string
  endAt: string | null
  visibilityZone: string
  latitude: number | null
  longitude: number | null
  visibilityRadiusKm: number | null
  status: string
  sourceName: string
  sourceUrl: string | null
}

export interface ObservationPlace {
  id: number
  name: string
  description: string
  latitude: number
  longitude: number
  lightPollutionLevel: number
  accessibilityScore: number
  distanceKm: number | null
  score: number | null
}

export function fetchMapEvents(): Promise<MapEvent[]> {
  return call('/api/events')
}

export function fetchRecommendedPlaces(
  eventId: number,
  latitude: number,
  longitude: number,
  maxDistanceKm = 200,
): Promise<ObservationPlace[]> {
  const params = new URLSearchParams({
    eventId: String(eventId),
    lat: String(latitude),
    lon: String(longitude),
    maxDistanceKm: String(maxDistanceKm),
    limit: '10',
  })

  return call(`/api/places/recommendations?${params}`)
}
