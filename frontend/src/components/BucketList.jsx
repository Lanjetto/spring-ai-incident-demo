import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchBuckets, sendMessage, fetchRawMessages, createIncident } from '../api.js'

const LEVEL_COLORS = {
  A1: '#d93025',
  A2: '#ea8600',
  A3: '#fbbc04',
  A4: '#34a853'
}

export default function BucketList() {
  const [buckets, setBuckets] = useState([])
  const [rawMessages, setRawMessages] = useState([])
  const [userId, setUserId] = useState('user-1')
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)

  const [selected, setSelected] = useState(new Set())
  const [modalOpen, setModalOpen] = useState(false)
  const [newTitle, setNewTitle] = useState('')
  const [newLevel, setNewLevel] = useState('A2')
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    setBuckets(await fetchBuckets())
    setRawMessages(await fetchRawMessages())
  }

  useEffect(() => {
    load()
    const t = setInterval(load, 5000)
    return () => clearInterval(t)
  }, [])

  async function onSubmit(e) {
    e.preventDefault()
    if (!message.trim()) return
    setSending(true)
    try {
      await sendMessage(userId, message)
      setMessage('')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setSending(false)
    }
  }

  function toggle(id) {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  async function onCreateIncident(e) {
    e.preventDefault()
    if (!newTitle.trim() || selected.size === 0) return
    setCreating(true)
    setError('')
    try {
      await createIncident(newTitle.trim(), newLevel, Array.from(selected))
      setSelected(new Set())
      setNewTitle('')
      setModalOpen(false)
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setCreating(false)
    }
  }

  return (
    <div>
      <h1>Бакеты инцидентов</h1>

      <form className="send-form" onSubmit={onSubmit}>
        <input value={userId} onChange={(e) => setUserId(e.target.value)} placeholder="ID пользователя" />
        <input value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Что не работает?" />
        <button disabled={sending}>Отправить</button>
      </form>

      {error && <div className="error">{error}</div>}

      <div className="buckets">
        {buckets.map((b) => (
          <Link key={b.level} to={`/bucket/${b.level}`} className="bucket" style={{ borderTopColor: LEVEL_COLORS[b.level] }}>
            <div className="bucket-level">{b.level}</div>
            <div className="bucket-label">{b.label}</div>
            <div className="bucket-desc">{b.description}</div>
            <div className="bucket-counts">
              инцидентов: <strong>{b.incidentCount}</strong> · сообщений: <strong>{b.messageCount}</strong>
            </div>
          </Link>
        ))}
      </div>

      <h2>Новые сообщения</h2>
      <p className="raw-hint">Отметьте сообщения и создайте из них инцидент вручную.</p>

      <div className="raw-messages">
        {rawMessages.length === 0 && <p className="raw-empty">Пока нет новых сообщений.</p>}
        {rawMessages.map((m) => (
          <label key={m.id} className={`raw-message${selected.has(m.id) ? ' selected' : ''}`}>
            <input type="checkbox" checked={selected.has(m.id)} onChange={() => toggle(m.id)} />
            <div className="raw-message-body">
              <div className="message-user">{m.userId}</div>
              <div className="message-text">{m.text}</div>
              <div className="message-time">{new Date(m.timestamp).toLocaleString()}</div>
            </div>
          </label>
        ))}
      </div>

      <button
        className="create-incident-btn"
        disabled={selected.size === 0}
        onClick={() => setModalOpen(true)}
      >
        Создать инцидент ({selected.size})
      </button>

      {modalOpen && (
        <div className="modal-overlay" onClick={() => setModalOpen(false)}>
          <form className="modal" onSubmit={onCreateIncident} onClick={(e) => e.stopPropagation()}>
            <h3>Новый инцидент</h3>
            <label className="modal-field">
              Название
              <input
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                placeholder="Например: Не приходят письма"
                autoFocus
              />
            </label>
            <label className="modal-field">
              Уровень критичности
              <select value={newLevel} onChange={(e) => setNewLevel(e.target.value)}>
                <option value="A1">A1 — FATAL: ресурс недоступен</option>
                <option value="A2">A2 — HIGH: серьёзная деградация</option>
                <option value="A3">A3 — MEDIUM: работа затруднена</option>
                <option value="A4">A4 — LOW: у пользователя проблемы</option>
              </select>
            </label>
            <div className="modal-actions">
              <button type="button" className="modal-cancel" onClick={() => setModalOpen(false)}>Отмена</button>
              <button type="submit" disabled={creating || !newTitle.trim()}>
                {creating ? 'Создание...' : 'Создать'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
