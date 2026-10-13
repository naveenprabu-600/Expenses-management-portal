# Adept Expense Portal — API

Spring Boot service behind the Adept Expense Portal: employees submit expense
claims, managers approve them, finance searches and imports them.

The web app is in [expense-portal-web](../expense-portal-web).

> This is a demonstration system. Run it on an internal network only; it is not
> hardened for exposure to the internet.

## Stack

Java 17, Spring Boot 4.1, PostgreSQL 16 (schema and seed data via Flyway),
Spring Security with signed bearer tokens, JSON logs (logstash-logback-encoder).

## Run everything with Docker

Both repositories must sit side by side:

```
expense-portal-api/
expense-portal-web/
```

```bash
cd expense-portal-api/deploy
cp .env.example .env        # then fill in every value
docker compose up -d --build
docker compose ps           # api and web should become "healthy"
```

The web app is then on `http://<WEB_BIND>:<WEB_PORT>` (by default
`http://127.0.0.1:8180`). The API is reachable only through the web app, at
`/api`.

| Service | What it is |
|---|---|
| `db` | PostgreSQL, data in the `db-data` volume |
| `api` | This service, built from this repository |
| `web` | The web app, built from `../expense-portal-web`, served by nginx |
| `filebeat` | Ships the API's log file to Elasticsearch |

To put the demo data back as first seeded: `./deploy/reset-demo-data.sh`.

## Configuration

Set in `deploy/.env` (see `.env.example`):

| Variable | Meaning |
|---|---|
| `APP_ENV` | Environment name, lower-case letters, e.g. `dev`. Names the log data stream and is added to every log line |
| `WEB_BIND`, `WEB_PORT` | Address and port the web app listens on |
| `DB_PASSWORD` | Database password |
| `EXPENSE_JWT_SECRET` | Signs access tokens; at least 32 characters. The API refuses to start without it |
| `DEMO_USER_PASSWORD` | Password given to every seeded account |
| `ELASTICSEARCH_HOSTS`, `ELASTICSEARCH_API_KEY` | Where Filebeat sends logs, and its write-only API key (`id:api_key`) |

## Demo accounts

All of them get `DEMO_USER_PASSWORD`. Accounts are created locked and unlocked
at start-up, so no password is stored in this repository.

| Username | Role | Department |
|---|---|---|
| `alice` | Employee | Engineering |
| `priya.raman` | Employee | Finance |
| `mark` | Manager | Engineering |
| `nina` | Manager | Finance |
| `fiona` | Finance | Finance |

## Logs

One JSON object per line on stdout. With the `log-file` profile (on in Docker)
the same lines also go to `/var/log/expense-portal/api.json`, which Filebeat
ships to the data stream `logs-expenseportal-<APP_ENV>`, using Elastic Common
Schema names:

| Field | Meaning |
|---|---|
| `@timestamp` | UTC |
| `log.level`, `log.logger`, `message` | Level, class that logged, text |
| `error.stack_trace` | Full stack trace for errors |
| `http.request.id` | The request ID. Every error response returns it as `referenceId`, and the web app shows it as the Reference ID |
| `user.name` | The signed-in user, when there is one |
| `service.name`, `service.environment` | `expense-portal-api` and `APP_ENV` |

## API

| Method and path | Who |
|---|---|
| `GET /api/auth/token` (HTTP Basic credentials) | Anyone: returns an access token |
| `GET /api/auth/me` | Signed in |
| `GET /api/claims`, `POST /api/claims`, `GET /api/claims/{id}` | Signed in |
| `POST`/`GET /api/claims/{id}/receipt` | Claimant (attach); claimant, approver or finance (download) |
| `GET /api/cost-centres` | Signed in |
| `GET /api/approvals`, `POST /api/approvals/{id}/approve`, `/reject` | Managers |
| `GET /api/search?q=`, `GET /api/search/summary` | Finance |
| `POST /api/imports` (XML) | Finance |
| `GET /actuator/health` | Anyone |

## Develop

```bash
./mvnw verify          # unit tests; no database or Docker needed
```

To run locally, start PostgreSQL and set `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`EXPENSE_JWT_SECRET` and `DEMO_USER_PASSWORD`, then `./mvnw spring-boot:run`.
