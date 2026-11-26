import { describe, expect, it } from 'vitest'
import { EMPTY_FILTERS, filterEvents } from './filters'

const events = [
  {
    id: 1,
    type: 'CONJUNCTION',
    visibilityZone: 'Île-de-France',
    startAt: '2026-12-20T19:00:00',
  },
  {
    id: 2,
    type: 'METEOR_SHOWER',
    visibilityZone: 'France métropolitaine',
    startAt: '2026-12-14T02:00:00',
  },
  {
    id: 3,
    type: 'CONJUNCTION',
    visibilityZone: 'Sud de la France',
    startAt: '2027-02-04T19:30:00',
  },
]

describe('Filtres des phénomènes astronomiques', () => {
  it('affiche tous les phénomènes sans filtre', () => {
    expect(filterEvents(events, EMPTY_FILTERS)).toHaveLength(3)
  })

  it('filtre par type de phénomène', () => {
    const result = filterEvents(events, {
      ...EMPTY_FILTERS,
      type: 'CONJUNCTION',
    })

    expect(result.map((e) => e.id)).toEqual([1, 3])
  })

  it('filtre par zone de visibilité', () => {
    const result = filterEvents(events, {
      ...EMPTY_FILTERS,
      location: 'Île-de-France',
    })

    expect(result.map((e) => e.id)).toEqual([1])
  })

  it('ignore la casse dans la recherche géographique', () => {
    const result = filterEvents(events, {
      ...EMPTY_FILTERS,
      location: 'FRANCE',
    })

    expect(result).toHaveLength(3)
  })

  it('filtre par date de début', () => {
    const result = filterEvents(events, {
      ...EMPTY_FILTERS,
      from: '2027-01-01',
    })

    expect(result.map((e) => e.id)).toEqual([3])
  })

  it('filtre par date de fin', () => {
    const result = filterEvents(events, {
      ...EMPTY_FILTERS,
      to: '2026-12-31',
    })

    expect(result.map((e) => e.id)).toEqual([1, 2])
  })

  it('combine plusieurs filtres', () => {
    const result = filterEvents(events, {
      type: 'CONJUNCTION',
      location: 'France',
      from: '2026-12-01',
      to: '2026-12-31',
    })

    expect(result.map((e) => e.id)).toEqual([1])
  })

  it('retourne une liste vide sans correspondance', () => {
    const result = filterEvents(events, {
      ...EMPTY_FILTERS,
      type: 'SOLAR_ECLIPSE',
    })

    expect(result).toEqual([])
  })

  it('restaure tous les phénomènes après réinitialisation', () => {
    const filtered = filterEvents(events, {
      ...EMPTY_FILTERS,
      type: 'CONJUNCTION',
    })

    expect(filtered).toHaveLength(2)
    expect(filterEvents(events, EMPTY_FILTERS)).toHaveLength(3)
  })
})
