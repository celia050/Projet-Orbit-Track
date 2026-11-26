export interface EventFilters {
  type: string
  location: string
  from: string
  to: string
}

export const EMPTY_FILTERS: EventFilters = {
  type: '',
  location: '',
  from: '',
  to: '',
}

interface FilterableEvent {
  type: string
  visibilityZone: string
  startAt: string
}

export function filterEvents<T extends FilterableEvent>(
  events: T[],
  filters: EventFilters,
): T[] {
  return events.filter((event) => {
    if (filters.type && event.type !== filters.type) {
      return false
    }

    if (
      filters.location &&
      !event.visibilityZone
        .toLocaleLowerCase('fr')
        .includes(filters.location.trim().toLocaleLowerCase('fr'))
    ) {
      return false
    }

    const date = new Date(event.startAt).getTime()

    if (
      filters.from &&
      date < new Date(filters.from + 'T00:00:00').getTime()
    ) {
      return false
    }

    if (
      filters.to &&
      date > new Date(filters.to + 'T23:59:59.999').getTime()
    ) {
      return false
    }

    return true
  })
}

export function synchronizeMapEvents<T extends { id: number }>(
  events: T[],
  outings: { eventId: number }[],
): T[] {
  const ids = new Set(outings.map((outing) => outing.eventId))
  return events.filter((event) => ids.has(event.id))
}
