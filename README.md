# EzDerm Appointment API

Spring Boot 4 / Java 21 project for appointment scheduling between doctors and patients.

## Requirements

- Java 21 JDK
- Rancher Desktop or another compatible Docker Compose runtime

## Modules

- `api-spec` - OpenAPI contract and generated controller interfaces/DTOs.
- `server` - Spring Boot application entry point and runtime configuration.
- `web` - REST controller implementation, request filter, and exception handling.
- `service` - Business services, mappers, domain exceptions, and use-case logic.
- `repository` - JPA entities, Spring Data repositories, and Liquibase changelogs.

## Run The App

### With Docker
Run from the repository root.

```powershell
# Start Rancher Desktop first, then run this command to bring up Postgres + the Spring Boot app
docker compose up --build -d
```

The API is exposed on `http://localhost:8080`.

### Without Docker

If you want to keep Postgres in Compose but run Spring Boot directly on the host:

```powershell
docker compose up -d postgres
./gradlew :server:bootRun
```

## Stop Or Reset Local DB

```
# Stop local containers
docker compose down

# Stop local containers and delete the Postgres volume
docker compose down -v
```

## Useful Database Commands

```
# Check whether Postgres is running and healthy
docker inspect -f "{{.State.Status}} {{.State.Health.Status}}" ezappointments-postgres

# List tables in the dev database
docker exec -it ezappointments-postgres psql -U ezappointments -d ezappointments -c "\dt"
```

## Docker Services

- `postgres` runs PostgreSQL 18.4 with a named volume for persisted local data.
- `app` builds from the repository `Dockerfile` and starts the Spring Boot jar on port `8080`.

## Gradle Build Commands

Run from the repository root.

```powershell
# Build all modules
./gradlew clean build

# Run all tests
./gradlew test

# Run code formatting
./gradlew spotlessApply
```

## Postman

Postman collection:

```text
etc/postman/ez_server.postman_collection.json
```

It contains requests for the local server and defaults to `http://localhost:8080`.

## Dev Data - Patient high load testing

Generate and import bulk patient data for local search testing:

```powershell
.\scripts\dev\seed-patients-bulk.ps1

# Smaller smoke run
.\scripts\dev\seed-patients-bulk.ps1 -Count 100000
```

## OpenAPI

Source spec and generator input: `api-spec/src/main/resources/openapi/api-spec-ezderm.yml`

Generated sources are written under: 
```api-spec/build/generated/openapi```

## API Assumptions

- Delete endpoints return `204 No Content`.
- Pagination uses zero-based `page` plus `size` query parameters.
- `409 Conflict` is used when deleting doctors or patients with active future appointments.
- `X-Username` is auth input and is required by all endpoints, must be in the specified format,
  and is assumed to correctly represent the requesting user’s identity.
- Creating an appointment gets the owning doctor from the request body, not from the X-Username header
