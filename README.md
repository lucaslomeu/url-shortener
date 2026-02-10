# URL Shortener

Shortens URLs and redirects users to the original.

---

## Configuration

The application uses a centralized configuration approach through `AppConfig.java`:

### Configuration Priority

1. **Environment Variables** (highest priority)
   - Used in production (Docker, Kubernetes, etc.)
   - Override any properties file values

2. **application.properties** (fallback)
   - Default values for local development
   - Located at `src/main/resources/application.properties`

### Available Settings

**Database:**

- `DB_URL` / `db.url` - PostgreSQL connection URL
- `DB_USER` / `db.user` - Database username
- `DB_PASSWORD` / `db.password` - Database password

**Redis:**

- `REDIS_HOST` / `redis.host` - Redis server hostname
- `REDIS_PORT` / `redis.port` - Redis server port

**Rate Limiting:**

- `RATE_LIMIT_REQUESTS_PER_MINUTE` / `rate.limit.requests.per.minute` - Requests per minute per IP (default: 100)

### Setup Instructions

**First-time setup:**

1. Copy example files:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties

cp .env.example .env
```

2. Edit the files with your local configuration:
   - `application.properties` - for local development without Docker
   - `.env` - for Docker Compose setup

---

## Endpoints

**Create short URL**

- `POST /api/v1/shorten`

**Redirect**

- `GET /{shortCode}` (public)
- `GET /api/v1/{shortCode}` (API)

**Health & Metrics**

- `GET /health`
- `GET /metrics`
