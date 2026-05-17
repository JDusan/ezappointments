# EzDerm Appointment API

Spring Boot 4 / Java 21 project for appointment scheduling between doctors and patients.

## Requirements

- Java 21 JDK
- Docker Desktop or compatible Docker Compose runtime
- Gradle Wrapper from this repository

## Gradle Commands

Run from the repository root.

```powershell
# Generate OpenAPI interfaces and DTOs
.\gradlew.bat :api-spec:openApiGenerate

# Compile generated API sources
.\gradlew.bat :api-spec:compileJava

# Build all modules
.\gradlew.bat clean build

# Run all tests
.\gradlew.bat test

# Format/check formatting
.\gradlew.bat spotlessApply
.\gradlew.bat spotlessCheck
```

On macOS/Linux, use `./gradlew` instead of `.\gradlew.bat`.

## Local Database

```powershell
# Start Postgres
docker compose up -d postgres

# Start Postgres plus pgAdmin
docker compose --profile tools up -d

# Stop local infra
docker compose down
```

Postgres dev settings:

- Database: `ezappointments`
- Username: `ezappointments`
- Password: `ezappointments`
- Port: `5432`

## Run The App

```powershell
docker compose up -d postgres
.\gradlew.bat :server:bootRun
```

The dev profile is the default profile. App port: `8080`.

## OpenAPI

Source spec:

- Root reference: `api-spec-ezderm.yml`
- Generator input: `api-spec/src/main/resources/openapi/api-spec-ezderm.yml`

Generated sources are written under:

```text
api-spec/build/generated/openapi
```

## API Assumptions

- Delete endpoints return `204 No Content`.
- Pagination uses classic zero-based `page` plus `size` query parameters.
- `409 Conflict` is used when deleting doctors or patients with active future appointments.
- `X-Username` is trusted auth input and is required by all endpoints.
