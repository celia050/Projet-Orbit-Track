import { describe, expect, it } from 'vitest'
import {
  EMPTY_FILTERS,
  filterEvents,
  synchronizeMapEvents,
} from './filters'

const events = [
  { id: 1, name: 'Conjonction Lune-Jupiter' },
  { id: 2, name: 'Pluie de météores' },
  { id: 3, name: 'Conjonction Vénus-Saturne' },
]

const outings = [
  {
    eventId: 1,
    type: 'CONJUNCTION',
    visibilityZone: 'Île-de-France',
    startAt: '2026-12-20T19:00:00',
  },
  {
    eventId: 2,
    type: 'METEOR_SHOWER',
    visibilityZone: 'France',
    startAt: '2026-12-14T02:00:00',
  },
  {
    eventId: 3,
    type: 'CONJUNCTION',
    visibilityZone: 'Sud de la France',
    startAt: '2027-02-04T19:30:00',
  },
]

describe('Synchronisation liste et carte', () => {
  it('affiche les mêmes phénomènes sans filtre', () => {
    expect(synchronizeMapEvents(events, outings)).toEqual(events)
  })

  it('affiche uniquement les phénomènes filtrés dans la liste', () => {
    const filtered = filterEvents(outings, {
      ...EMPTY_FILTERS,
      type: 'CONJUNCTION',
    })

    const result = synchronizeMapEvents(events, filtered)

    expect(result.map((event) => event.id)).toEqual([1, 3])
  })

  it('affiche une carte sans phénomène si la liste est vide', () => {
    expect(synchronizeMapEvents(events, [])).toEqual([])
  })

  it('exclut les phénomènes absents de la liste', () => {
    expect(
      synchronizeMapEvents(events, [{ eventId: 2 }]),
    ).toEqual([events[1]])
  })

  it('gère les identifiants absents des données cartographiques', () => {
    expect(
      synchronizeMapEvents(events, [{ eventId: 99 }]),
    ).toEqual([])
  })
})
