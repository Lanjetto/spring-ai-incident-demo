# Incident Buckets

MVP по Spring AI: приём сообщений пользователей, кластеризация их в инциденты,
группировка инцидентов в бакеты по уровню критичности (A1–A4) и генерация
контекстных подсказок по решению через RAG по историческим инцидентам.

```
Пользователь --> POST /api/messages --> Kafka (user-messages)
                                        --> Консьюмер:
                                            1. LLM классифицирует критичность (A1..A4)
                                            2. LLM относит к существующему инциденту или создаёт новый
                                            3. Сообщение сохраняется, инцидент эскалируется по числу сообщений
Интерфейс --> GET /api/buckets, /api/buckets/{level}/incidents, /api/incidents/{id}
Интерфейс --> POST /api/incidents/{id}/hint  (RAG: pgvector + Qwen3-32B)
```

- **Backend**: Java 21, Spring Boot 4.1.0, Spring AI 2.0.0, Kafka, JPA, Liquibase
- **AI**: chat — Qwen3-32B (OpenAI-совместимый endpoint), embedding — mxbai-embed-large (Ollama), vector — pgvector
- **Frontend**: React 18 + Vite

---

## Быстрый старт

### 1. Инфраструктура (PostgreSQL+pgvector и Kafka)

| Сервис | Контейнер | Порт |
|--------|-----------|------|
| PostgreSQL + pgvector | `demo-pgvector` | 5433 |
| Kafka | `kafka` | 9092 |
| Ollama| `demo_llm-ollama-1` | 11434 |

Для чистой среды — поднять всё из docker-compose:
```
docker compose up -d
```

### 2. Backend

```
cd backend
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

При старте происходит:
- **Liquibase** создаёт схему и сидирует **300 исторических инцидентов** с решениями;
- **KnowledgeVectorSeeder** генерирует их эмбеддинги через Ollama и кладёт в pgvector
  (только если хранилище пусто).

Backend поднимется на **http://localhost:8080**.

### 3. Frontend

```
cd frontend
npm install
npm run dev
```
Открыть **http://localhost:5173**. Vite проксирует `/api` на `http://localhost:8080`.

### 4. Накидать тестовых сообщений

```
./scripts/send-test-messages.ps1    # Windows (PowerShell)
./scripts/send-test-messages.sh     # WSL/Linux (bash)
```

Что делают скрипты: шлют несколько серий сообщений разной критичности, включая
**35 сообщений про «сервер не отвечает»** — чтобы наглядно увидеть эскалацию A4→A1.
Обработка асинхронная, поэтому вкладка интерфейса обновляется сама раз в 5 секунд.

---

## Структура проекта

```
incident-buckets/
├── CONTEXT.md                 # Доменная модель (глоссарий терминов)
├── docker-compose.yml         # PostgreSQL+pgvector + Kafka (для чистой среды)
├── docs/adr/                  # Architectural Decision Records
├── backend/                   # Spring Boot приложение
│   ├── build.gradle           # Зависимости (Spring Boot 4.1.0, Spring AI 2.0.0)
│   ├── .env                   # Креды и адреса (импортируются в application.yml)
│   └── src/
│       ├── main/java/com/example/incidentbuckets/
│       │   ├── IncidentBucketsApplication.java   # Точка входа + @EnableConfigurationProperties
│       │   ├── config/         # Свойства приложения + сидер pgvector
│       │   ├── domain/         # JPA-сущности: Incident, UserMessage, KnowledgeDoc, SeverityLevel
│       │   ├── repository/     # Spring Data JPA репозитории
│       │   ├── service/        # IncidentService — оркестрация обработки сообщения
│       │   ├── ai/             # Spring AI: классификация, отнесение, RAG-подсказка
│       │   ├── kafka/          # Продюсер, консьюмер, событие, конфиг топика
│       │   └── web/            # REST-контроллеры + GlobalExceptionHandler
│       │       └── dto/        # Records запросов/ответов (UserMessageRequest, *Response, ApiError)
│       └── main/resources/
│           ├── application.yml                 # Конфигурация Spring + AI + Kafka
│           ├── db/changelog/                   # Liquibase: схема + сид 300 инцидентов
│           ├── prompts/                        # Шаблоны промптов LLM (.st)
│           └── scripts/gen_seed.py             # Генератор сид-данных (002-seed-knowledge.yaml)
├── frontend/                  # React + Vite
│   ├── vite.config.js         # Прокси /api -> localhost:8080
│   └── src/
│       ├── api.js             # fetch-обёртки над REST API
│       ├── App.jsx            # Роутинг: / , /bucket/:level , /incident/:id
│       └── components/        # BucketList, BucketDetail, IncidentDetail
└── scripts/                   # Тестовые скрипты
```

---

## Как это работает

### Поток обработки сообщения

1. `POST /api/messages` (`MessageController`) принимает `{userId, message, timestamp}`,
   публикует событие `UserMessageEvent` в Kafka-топик `user-messages` и сразу
   возвращает **202 Accepted** с `messageId`.
2. Консьюмер `UserMessageConsumer` вызывает `IncidentService.processMessage()`:
   - **Классификация** (`ClassificationService` + `classify.st`): LLM ставит уровень A1–A4.
     Нераспознанный ответ → A4.
   - **Отнесение** (`AssignmentService` + `assign.st`): LLM смотрит список последних
     инцидентов этого же уровня и решает — это тот же инцидент (вернёт UUID) или новая
     проблема (вернёт NEW).
   - Инцидент сохраняется/обновляется: растёт `messageCount`, вызывается эскалация.
3. **Эскалация** (`Incident.applyEscalation`): каждые `escalation-step-messages` (по умолчанию
   10) сообщений в инциденте поднимают критичность на одну ступень к A1. Начальный
   уровень всегда ставит LLM; вниз критичность не опускается.

### RAG-подсказка

`POST /api/incidents/{id}/hint` (`IncidentController` + `HintService` + `hint.st`):

1. Берутся все сообщения инцидента как поисковый запрос.
2. `VectorStore.similaritySearch()` ищет похожие исторические инциденты в pgvector.
3. Найденные `симптом + решение` подставляются в промпт, LLM генерирует подсказку
   оператору.
4. Подсказка **кэшируется** в поле `incident.hint` — повторный запрос не трогает LLM.

### Доменная модель (`CONTEXT.md`)

- **UserMessage** — сырой отчёт `{userId, text, receivedAt}`, уровня критичности не имеет.
- **Incident** — кластер смыслово похожих сообщений (одна проблема), владеет уровнем.
- **Bucket** — срез инцидентов по `SeverityLevel` (A1–A4), отдельной сущности нет.
- **SeverityLevel** — A1 (FATAL) … A4 (LOW). Меньший номер = критичнее.
- **KnowledgeDoc** — исторический инцидент с решением, база для RAG.

---

## Конфигурация

Все настройки вынесены наружу.

**`backend/.env`** (импортируется через `spring.config.import` в application.yml):

| Переменная | Назначение |
|------------|------------|
| `OPENAI_API_KEY` | Ключ для OpenAI-совместимого endpoint |
| `OPENAI_BASE_URL` | Базовый URL чат-модели  |
| `OPENAI_CHAT_MODEL` | Модель чата  |
| `OLLAMA_BASE_URL` | Адрес Ollama (по умолчанию `http://localhost:11434`) |
| `OLLAMA_EMBEDDING_MODEL` | Эмбеддинг-модель (по умолчанию `mxbai-embed-large`) |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Подключение к PostgreSQL |
| `KAFKA_BOOTSTRAP_SERVERS` | Адрес Kafka |
| `INCIDENT_TOPIC` | Kafka-топик (по умолчанию `user-messages`) |

**`application.yml`** — секция `incident-buckets`:

| Свойство | По умолчанию | Описание |
|----------|-------------|----------|
| `incident-buckets.escalation-step-messages` | 10 | Через сколько сообщений уровень растёт на ступень |
| `incident-buckets.assignment-candidates` | 10 | Сколько инцидентов-кандидатов показывать LLM при отнесении |
| `incident-buckets.rag-top-k` | 4 | Сколько похожих записей из KB использовать в подсказке |

---

## REST API

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/messages` | Приём сообщения `{userId, message, timestamp}` → 202 + messageId |
| GET | `/api/buckets` | Четыре бакета с количеством инцидентов и сообщений |
| GET | `/api/buckets/{level}/incidents` | Инциденты бакета уровня (A1–A4) |
| GET | `/api/incidents/{id}` | Инцидент со всеми сообщениями |
| POST | `/api/incidents/{id}/hint` | Сгенерировать (и закэшировать) RAG-подсказку ИИ |


Примеры запросов: `scripts/examples.ps1`.

---

## Скрипты

| Скрипт | Для чего |
|--------|----------|
| `scripts/send-test-messages.ps1` | Накидать тестовых сообщений (Windows) — включает 35 сообщений для демонстрации эскалации |
| `scripts/send-test-messages.sh` | То же для WSL/Linux |
| `scripts/examples.ps1` | Примеры curl-запросов ко всем endpoint'ам |
| `backend/src/main/resources/scripts/gen_seed.py` | Генератор 300 записей исторических инцидентов для Liquibase-сида |

