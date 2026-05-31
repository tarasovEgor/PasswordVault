# Password Vault Scala

## Stack

- Scala 3
- cats-effect
- http4s
- circe
- doobie
- PostgreSQL
- Flyway
- Docker Compose

## Run PostgreSQL

docker compose up -d db

## Run migrations

docker compose run --rm flyway

## Environment variables

export DB_URL="jdbc:postgresql://127.0.0.1:5432/password_vault"
export DB_USER="password_vault"
export DB_PASSWORD="password_vault"
export MASTER_KEY_BASE64="$(openssl rand -base64 32)"

Important: keep the same MASTER_KEY_BASE64 between application restarts.

## Run app

sbt run

## API examples

curl http://localhost:8080/health

curl -X POST http://localhost:8080/passwords \
-H "Content-Type: application/json" \
-d '{"name":"GitHub","password":"secret","comment":"dev account"}'

curl http://localhost:8080/passwords