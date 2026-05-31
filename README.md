# Password Vault

REST API service for securely storing and managing passwords, built with Scala 3 and Typelevel stack.

## Tech Stack

- **Scala 3** + **Cats Effect 3** — async runtime
- **http4s** — HTTP server
- **tapir** — OpenAPI/Swagger documentation
- **circe** — JSON serialization
- **Doobie** — functional database access
- **PostgreSQL** — storage
- **Flyway** — database migrations
- **AES-GCM** — passwords are encrypted at rest

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d db
```

### 2. Run migrations

```bash
docker-compose run --rm flyway
```

### 3. Set environment variables

```bash
export DB_URL="jdbc:postgresql://127.0.0.1:5432/password_vault"
export DB_USER="password_vault"
export DB_PASSWORD="password_vault"
export MASTER_KEY_BASE64="$(openssl rand -base64 32)"
```

> Keep the same `MASTER_KEY_BASE64` between restarts — it is used to encrypt and decrypt stored passwords.

### 4. Run the application

```bash
sbt run
```

Service starts at `http://localhost:8080`.
Swagger UI is available at `http://localhost:8080/docs`.

## API Reference

### Health

| Method | Endpoint | Description  |
|--------|----------|--------------|
| GET    | /health  | Health check |

### Passwords

| Method | Endpoint                          | Description                     |
|--------|-----------------------------------|---------------------------------|
| GET    | /passwords                        | List all entries                |
| POST   | /passwords                        | Create a new entry              |
| GET    | /passwords/:id                    | Get entry by ID                 |
| PATCH  | /passwords/:id                    | Update entry                    |
| DELETE | /passwords/:id                    | Delete entry (soft delete)      |
| GET    | /passwords?search=X               | Search by partial name match    |
| GET    | /passwords?search=X&exact=true    | Search by exact name match      |
| GET    | /passwords/export                 | Export all entries as CSV       |
| POST   | /passwords/import                 | Import entries from CSV         |

## API Examples

**Create**
```bash
curl -X POST http://localhost:8080/passwords \
  -H "Content-Type: application/json" \
  -d '{"name":"GitHub","password":"secret123","comment":"dev account"}'
```

**Search**
```bash
curl "http://localhost:8080/passwords?search=git"
curl "http://localhost:8080/passwords?search=GitHub&exact=true"
```

**Update**
```bash
curl -X PATCH http://localhost:8080/passwords/1 \
  -H "Content-Type: application/json" \
  -d '{"password":"newpassword"}'
```

**Delete**
```bash
curl -X DELETE http://localhost:8080/passwords/1
```

**Export CSV**
```bash
curl http://localhost:8080/passwords/export -o passwords.csv
```

**Import CSV**
```bash
curl -X POST http://localhost:8080/passwords/import \
  -H "Content-Type: text/plain" \
  --data-binary @passwords.csv
```