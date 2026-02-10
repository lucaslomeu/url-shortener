# URL Shortener

Shortens URLs and redirects users to the original.

---

## Configuration

The application uses a centralized configuration approach through `AppConfig.java`:

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
