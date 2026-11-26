import { useState } from 'react'

interface CalendarButtonProps {
  eventId: number
}

export default function CalendarButton({ eventId }: CalendarButtonProps) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function downloadCalendar() {
    setLoading(true)
    setError(null)

    try {
      const response = await fetch(`/api/events/${eventId}/calendar`)

      if (!response.ok) {
        throw new Error('Impossible de télécharger cet événement.')
      }

      const blob = await response.blob()
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')

      link.href = url
      link.download = `orbittrack-${eventId}.ics`

      document.body.appendChild(link)
      link.click()
      link.remove()

      window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch {
      setError('Le téléchargement a échoué. Veuillez réessayer.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <button
        type="button"
        disabled={loading}
        onClick={downloadCalendar}
        title="Télécharger un fichier compatible avec votre calendrier"
      >
        {loading ? 'Téléchargement…' : 'Ajouter à mon agenda'}
      </button>

      {error && (
        <span className="auth-error" role="alert">
          {error}
        </span>
      )}
    </>
  )
}
