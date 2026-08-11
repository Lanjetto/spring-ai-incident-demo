import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchIncidents } from '../api.js'

export default function BucketDetail() {
  const { level } = useParams()
  const [incidents, setIncidents] = useState([])

  useEffect(() => {
    let alive = true
    async function load() {
      const data = await fetchIncidents(level)
      if (alive) setIncidents(data)
    }
    load()
    const t = setInterval(load, 5000)
    return () => {
      alive = false
      clearInterval(t)
    }
  }, [level])

  return (
    <div>
      <Link to="/">← Бакеты</Link>
      <h1>Бакет {level}</h1>

      {incidents.length === 0 && <p>Пока нет инцидентов этого уровня.</p>}

      <div className="incidents">
        {incidents.map((i) => (
          <Link key={i.id} to={`/incident/${i.id}`} className="incident">
            <div className="incident-title">{i.title}</div>
            <div className="incident-meta">
              {i.messageCount} сообщений · обновлён {new Date(i.lastMessageAt).toLocaleString()}
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
