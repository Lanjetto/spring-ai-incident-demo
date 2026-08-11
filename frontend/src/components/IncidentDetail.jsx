import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchIncident, generateHint, closeIncident } from '../api.js'

export default function IncidentDetail() {
  const { id } = useParams()
  const [incident, setIncident] = useState(null)
  const [hintLoading, setHintLoading] = useState(false)
  const [hint, setHint] = useState(null)
  const [resolution, setResolution] = useState('')
  const [closing, setClosing] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let alive = true
    async function load() {
      const data = await fetchIncident(id)
      if (alive) {
        setIncident(data)
        setHint((prev) => {
          if (prev) return prev
          if (!data.hint) return null
          return { steps: data.hint.split('\n').map((l) => l.trim()).filter(Boolean), similarIncidents: [] }
        })
      }
    }
    load()
    const t = setInterval(load, 5000)
    return () => {
      alive = false
      clearInterval(t)
    }
  }, [id])

  async function onHint() {
    setHintLoading(true)
    setError('')
    try {
      const data = await generateHint(id)
      setHint(data)
    } catch (e) {
      setError(e.message)
    } finally {
      setHintLoading(false)
    }
  }

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
        Уровень: <strong>{incident.level}</strong> (изначально {incident.baseLevel}) ·{' '}
        {incident.messageCount} сообщений · создан {new Date(incident.createdAt).toLocaleString()}
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

      <h2>Подсказка ИИ</h2>
      {hint ? (
        <div className="hint">
          {hint.steps.length > 0 && (
            <ol>
              {hint.steps.map((s, i) => (
                <li key={i}>{s.replace(/^\d+[.)]\s*/, '')}</li>
              ))}
            </ol>
          )}
          {hint.similarIncidents.length > 0 && (
            <div className="similar">
              <h3>Похожие инциденты из базы знаний</h3>
              {hint.similarIncidents.map((s) => (
                <div key={s.id} className="similar-incident">
                  <div className="similar-title">{s.title}</div>
                  <div className="similar-symptom">{s.symptom}</div>
                  {s.resolution && <div className="similar-resolution">Решение: {s.resolution}</div>}
                </div>
              ))}
            </div>
          )}
          {hint.steps.length === 0 && hint.similarIncidents.length === 0 && <p>Подсказка пуста.</p>}
        </div>
      ) : incident.open ? (
        <button onClick={onHint} disabled={hintLoading}>
          {hintLoading ? 'Генерация...' : 'Получить подсказку'}
        </button>
      ) : (
        <p>Нет подсказки.</p>
      )}

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
