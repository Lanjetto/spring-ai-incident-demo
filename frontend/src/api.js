export async function fetchBuckets() {
  const res = await fetch('/api/buckets')
  if (!res.ok) throw new Error('Не удалось загрузить бакеты')
  return res.json()
}

export async function fetchIncidents(level) {
  const res = await fetch(`/api/buckets/${level}/incidents`)
  if (!res.ok) throw new Error('Не удалось загрузить инциденты')
  return res.json()
}

export async function fetchIncident(id) {
  const res = await fetch(`/api/incidents/${id}`)
  if (!res.ok) throw new Error('Не удалось загрузить инцидент')
  return res.json()
}

export async function generateHint(id) {
  const res = await fetch(`/api/incidents/${id}/hint`, { method: 'POST' })
  if (!res.ok) throw new Error('Не удалось получить подсказку')
  return res.json()
}

export async function closeIncident(id, resolution) {
  const res = await fetch(`/api/incidents/${id}/close`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ resolution })
  })
  if (!res.ok) throw new Error('Не удалось закрыть инцидент')
  return res.json()
}

export async function sendMessage(userId, message) {
  const res = await fetch('/api/messages', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, message, timestamp: new Date().toISOString() })
  })
  if (!res.ok) throw new Error('Не удалось отправить сообщение')
  return res.json()
}
