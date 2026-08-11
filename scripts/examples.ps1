# Примеры ручных запросов к API.

BASE=http://localhost:8080

# Отправить одно сообщение
curl -X POST $BASE/api/messages \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-1","message":"Не работает сервер, ошибка 500 на всех запросах","timestamp":"2026-08-10T10:00:00Z"}'

# Список бакетов (A1-A4)
curl $BASE/api/buckets

# Инциденты в бакете A1
curl $BASE/api/buckets/A1/incidents

# Инцидент целиком с сообщениями (подставить id)
curl $BASE/api/incidents/00000000-0000-0000-0000-000000000000

# Сгенерировать RAG-подсказку ИИ по инциденту
curl -X POST $BASE/api/incidents/00000000-0000-0000-0000-000000000000/hint
