import { typeLabel } from '../sorties/api'
import { EMPTY_FILTERS, type EventFilters as Filters } from './filters'

interface Props {
  filters: Filters
  types: string[]
  onChange: (filters: Filters) => void
}

export function EventFilters({ filters, types, onChange }: Props) {
  return (
    <div className="form-card">
      <h2>Filtrer les phénomènes</h2>

      <div className="form-row">
        <div>
          <label htmlFor="event-type">Type de phénomène</label>
          <select
            id="event-type"
            value={filters.type}
            onChange={(e) =>
              onChange({ ...filters, type: e.target.value })
            }
          >
            <option value="">Tous les types</option>
            {types.map((type) => (
              <option key={type} value={type}>
                {typeLabel(type)}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="event-location">Zone de visibilité</label>
          <input
            id="event-location"
            placeholder="Ex. France"
            value={filters.location}
            onChange={(e) =>
              onChange({ ...filters, location: e.target.value })
            }
          />
        </div>
      </div>

      <div className="form-row">
        <div>
          <label htmlFor="event-from">Date de début</label>
          <input
            id="event-from"
            type="date"
            value={filters.from}
            onChange={(e) =>
              onChange({ ...filters, from: e.target.value })
            }
          />
        </div>

        <div>
          <label htmlFor="event-to">Date de fin</label>
          <input
            id="event-to"
            type="date"
            min={filters.from || undefined}
            value={filters.to}
            onChange={(e) =>
              onChange({ ...filters, to: e.target.value })
            }
          />
        </div>
      </div>

      <button
        type="button"
        onClick={() => onChange({ ...EMPTY_FILTERS })}
      >
        Réinitialiser les filtres
      </button>
    </div>
  )
}
