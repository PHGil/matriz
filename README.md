# Matriz

Project and task management application for work teams (similar to tools such as Jira, Azure DevOps or Trello).

## Problem

Teams need a single place to organise projects, distribute tasks and understand the progress of their work. Without it, work is scattered, it is unclear who is responsible for what, and it is hard to know how much is left to finish a project.

## Main goals

- Organise work into projects with access restricted to their members.
- Create, assign and update tasks.
- Track the progress of each project.

## Planned MVP features

- Email and password authentication; accounts managed by an Administrator.
- Project creation and member management.
- Tasks with title, description, assignee, priority, due date and status (To Do / In Progress / Done).
- Task board by status.
- Progress indicator per project (percentage of tasks done).

Details in [docs/requirements.md](docs/requirements.md#5-mvp-scope).

## Documentation

| File                                                       | Contents                                                                                     |
| ---------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| [docs/requirements.md](docs/requirements.md)               | Vision, user profiles, functional and non-functional requirements, MVP scope.                |
| [docs/user-stories.md](docs/user-stories.md)               | Backlog, user stories, acceptance criteria and exceptional cases.                            |
| [docs/constraints.md](docs/constraints.md)                 | Constraints, assumptions, security and privacy, pending decisions, questions for the client. |
| [docs/architecture.md](docs/architecture.md)               | Architecture decision (modular monolith), components, ADRs, consistency review.              |
| [docs/database.md](docs/database.md)                       | Data model, business rules and Mermaid ER diagram.                                           |
| [openapi.yaml](openapi.yaml)                               | OpenAPI 3.1 contract of the MVP API (authentication via Keycloak).                           |
| [docs/ui/](docs/ui/)                                       | Low-fidelity wireframe of the project board (`wireframe.svg` / `wireframe.png`).             |
| [docs/implementation-plan.md](docs/implementation-plan.md) | Scope reconciliation, access rules, technical decisions and increments (Class 3).            |
| [AGENTS.md](AGENTS.md)                                     | Instructions for AI agents working in this repository.                                       |

## Current status

**Implementation phase (Class 3).** Progress per increment in [docs/implementation-plan.md](docs/implementation-plan.md#4-increments).

**Stack:** Java 17 + Quarkus 3.40 LTS (REST API), Angular 22 (SPA), PostgreSQL 18, Keycloak 26 (authentication).

## Running locally

### Requirements

- Docker with Docker Compose
- Java 17+ (Maven is provided by the wrapper `./mvnw`)
- Node.js 26 LTS (`nvm use` reads [.nvmrc](.nvmrc))

### 1. Configuration

```bash
cp .env.example .env          # fictitious local values; .env is never committed
ln -s ../.env backend/.env    # Quarkus reads .env from its working directory
```

PostgreSQL is exposed on port **5432** and Keycloak on **8180** (change `DB_PORT` in `.env` if needed).

### 2. Infrastructure (PostgreSQL + Keycloak)

```bash
docker compose up -d
```

- Creates the `matriz` database and a separate `keycloak` database on first start.
- Imports the Keycloak realm `matriz` from [keycloak/matriz-realm.json](keycloak/matriz-realm.json) only if it does not exist yet.
- Keycloak console: <http://localhost:8180/admin/matriz/console> (realm administrator `admin@matriz.test`).

### 3. Backend (schema and demo data)

```bash
cd backend
./mvnw quarkus:dev            # http://localhost:8080 — API under /api/v1, health at /q/health
```

On start, Flyway applies the schema ([V1__schema.sql](backend/src/main/resources/db/migration/V1__schema.sql)) and, in the `dev` profile only, the demo data ([R__demo_data.sql](backend/src/main/resources/db/seed/R__demo_data.sql)). The seed is idempotent: restarting never wipes or duplicates data.

### 4. Frontend

```bash
cd frontend
npm install
npm start                     # http://localhost:4200 — /api is proxied to the backend
```

### Authentication (Keycloak)

1. Opening the app redirects to the Keycloak login page of realm `matriz` (OIDC Authorization Code + PKCE, client `matriz-frontend`).
2. After login, the app sends the access token as `Authorization: Bearer <token>` only to `/api/v1`. The backend validates the JWT (issuer, signature, audience `matriz-backend`) and identifies the user by its `sub` claim.
3. Access tokens last **5 minutes** and are refreshed automatically while the user is active; after **30 minutes** of inactivity (or 8 h in total) the Keycloak session ends.
4. **Sair** logs out of Keycloak and returns to the login page. If the API answers `401`, the app shows "A sua sessão expirou" with a button to log in again.
5. Accounts are created, disabled and re-enabled in the Keycloak console. The backend mirrors users into `app_user` at startup and every 5 minutes; a disabled user cannot log in and loses API access when the current token expires (≤ 5 min).

### Demo accounts

All demo users share the password set in `MATRIZ_DEMO_PASSWORD` in your `.env`.

| User                | Role                                                 |
| ------------------- | ---------------------------------------------------- |
| `ana@matriz.test`   | Manager of "Website 2027"                            |
| `bruno@matriz.test` | Member of "Website 2027", Manager of "Intranet"      |
| `carla@matriz.test` | Member of "Intranet" (no access to "Website 2027")   |
| `admin@matriz.test` | Realm administrator in Keycloak (accounts, FR-03/04) |

### Tests

```bash
cd backend && ./mvnw test     # needs Docker (PostgreSQL started by Quarkus Dev Services)
cd frontend && npm test -- --watch=false
```

### Reset local data

```bash
docker compose down -v        # deletes the PostgreSQL volume (application and Keycloak data)
```
