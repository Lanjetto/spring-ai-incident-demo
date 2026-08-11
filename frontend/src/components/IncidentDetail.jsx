import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchIncident, closeIncident } from '../api.js'

export default function IncidentDetail() {
  const { id } = useParams()
  const [incident, setIncident] = useState(null)
  const [resolution, setResolution] = useState('')
  const [closing, setClosing] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let alive = true
    async function load() {
      const data = await fetchIncident(id)
      if (alive) setIncident(data)
    }
    load()
    const t = setInterval(load, 5000)
    return () => {
      alive = false
      clearInterval(t)
    }
  }, [id])

  async function onClose(e) {
    e.preventDefault()
    if (!resolution.trim()) return
    setClosing(true)
    setError('')
    try {
      const updated = await closeIncident(id, resolution)
      setIncident(updated)
    } catch (err) {
      setError(err.message)
    } finally {
      setClosing(false)
    }
  }

  if (!incident) return <p>Загрузка...</p>

  return (
    <div>
      <Link to={`/bucket/${incident.level}`}>← Бакет {incident.level}</Link>
      <h1>{incident.title}</h1>
      <div className="incident-meta">
        Уровень: <strong>{incident.level}</strong> · {incident.messageCount} сообщений · создан{' '}
        {new Date(incident.createdAt).toLocaleString()}
        {!incident.open && ` · закрыт ${new Date(incident.closedAt).toLocaleString()}`}
      </div>

      {error && <div className="error">{error}</div>}

      <h2>Сообщения пользователей</h2>
      <div className="messages">
        {incident.messages.map((m) => (
          <div key={m.id} className="message">
            <div className="message-user">{m.userId}</div>
            <div className="message-text">{m.text}</div>
            <div className="message-time">{new Date(m.receivedAt).toLocaleString()}</div>
          </div>
        ))}
      </div>

      {incident.open ? (
        <form className="close-form" onSubmit={onClose}>
          <h2>Закрыть инцидент</h2>
          <textarea
            value={resolution}
            onChange={(e) => setResolution(e.target.value)}
            placeholder="Опишите решение проблемы"
            rows="3"
          />
          <button disabled={closing || !resolution.trim()}>
            {closing ? 'Закрытие...' : 'Закрыть инцидент'}
          </button>
        </form>
      ) : (
        incident.resolution && (
          <div className="resolution">
            <h2>Решение</h2>
            <p>{incident.resolution}</p>
          </div>
        )
      )}
    </div>
  )
}
