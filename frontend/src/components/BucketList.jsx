import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchBuckets, sendMessage } from '../api.js'

const LEVEL_COLORS = {
  A1: '#d93025',
  A2: '#ea8600',
  A3: '#fbbc04',
  A4: '#34a853'
}

export default function BucketList() {
  const [buckets, setBuckets] = useState([])
  const [userId, setUserId] = useState('user-1')
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)

  async function load() {
    setBuckets(await fetchBuckets())
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
    } finally {
      setSending(false)
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
    </div>
  )
}
