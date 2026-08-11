# Incident Bucket Start

Стартовая версия без ИИ: ручная кластеризация сообщений в инциденты оператором.

```
Пользователь --> POST /api/messages --> raw_messages (сырые сообщения)
Оператор --> отмечает сообщения на главной --> POST /api/incidents
             {title, level, messageIds} --> сообщения переезжают в user_message,
             инцидент попадает в бакет A1–A4
Интерфейс --> GET /api/buckets, /api/buckets/{level}/incidents, /api/incidents/{id}
```

- **Backend**: Java 21, Spring Boot 4.1.0, JPA
- **Frontend**: React 18 + Vite

---

## Быстрый старт

### 1. Инфраструктура (PostgreSQL)

На этой машине уже запущена в WSL (`demo-pgvector`, порт 5433, БД `incident_buckets`).
Схема создаётся вручную (Liquibase отключён):

```sql
CREATE TABLE IF NOT EXISTS raw_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL,
  text TEXT NOT NULL,
  timestamp TIMESTAMPTZ NOT NULL
);
```

### 2. Backend

```
cd backend
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

Backend поднимется на **http://localhost:8080**. ИИ нет, Kafka нет, Liquibase отключён.

### 3. Frontend

```
cd frontend
npm install
npm run dev
```
Открыть **http://localhost:5173**. Vite проксирует `/api` на `http://localhost:8080`.

### 4. Как пользоваться

1. Напишите сообщение на главной — оно появится в списке «Новые сообщения».
2. Отметьте нужные сообщения чекбоксами.
3. Нажмите «Создать инцидент», в модалке дайте название и уровень критичности A1–A4.
4. Карточка инцидента попадёт в бакет, выбранные сообщения исчезнут со главной.

---

## REST API

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/messages` | Приём сообщения `{userId, message, timestamp}` → 202 + messageId |
| GET | `/api/messages` | Список сырых сообщений (не отнесённых к инцидентам) |
| POST | `/api/incidents` | Создание инцидента `{title, level, messageIds}` из сырых сообщений |
| GET | `/api/buckets` | Четыре бакета с количеством инцидентов и сообщений |
| GET | `/api/buckets/{level}/incidents` | Инциденты бакета уровня (A1–A4) |
| GET | `/api/incidents/{id}` | Инцидент со всеми сообщениями |
| POST | `/api/incidents/{id}/close` | Закрытие инцидента `{resolution}` |

Ошибки возвращаются единым форматом:
```json
{"status":400,"error":"Bad Request","message":"Тело запроса не читается: проверьте формат JSON","timestamp":"..."}
```

---

## Конфигурация

Все настройки вынесены наружу.

**`backend/.env`** (импортируется через `spring.config.import` в application.yml):

| Переменная | Назначение |
|------------|------------|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Подключение к PostgreSQL |

Liquibase отключён (`spring.liquibase.enabled: false`) — схема не создаётся и не сидируется автоматически.

---

## ADR (ключевые решения)

- `0001` — Бакет — это уровень критичности, а не отдельная сущность
