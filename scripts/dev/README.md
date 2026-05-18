# Dev Scripts

## seed-million-patients.ps1

Generates fake patient rows into a CSV file into build/dev-data/patients.csv, copies the CSV into the Postgres container, imports it with PostgreSQL `COPY`, then runs `ANALYZE patient` so search queries use fresh planner statistics.

This is for local performance testing of patient search with large datasets. It does not change application code or Liquibase migrations.

## Prerequisites

Start the local database first:

```powershell
docker compose up -d postgres
```

The script expects the default container from `compose.yml`:

```text
ezappointments-postgres
```

## Run

Small smoke run:

```powershell
.\scripts\dev\seed-million-patients.ps1 -Count 10000
```

Full default run:

```powershell
.\scripts\dev\seed-million-patients.ps1
```

The default inserts `1,000,000` patients.
