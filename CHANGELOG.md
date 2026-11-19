# Changelog

All notable changes to Matriz. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow [Semantic Versioning](https://semver.org/). Before 1.0.0, a minor version may contain any change.

## [0.2.0] - Unreleased (release candidate)

Release notes: [docs/release-notes-v0.2.0.md](docs/release-notes-v0.2.0.md).

### Added

- Render Blueprint for three free container services and a production Keycloak image without local demo accounts/test client; Neon TLS setup and deployment checklist in `docs/deployment.md`.
- Runtime frontend `KEYCLOAK_URL` configuration and optional backend `DB_JDBC_URL` for cloud database connections.
- Production realm regression checks and Keycloak image build in CI.
- Full application in containers: multi-stage Dockerfiles for the backend and the frontend (built from source), and the `app` profile in `compose.yaml` with healthchecks for PostgreSQL, Keycloak, backend and frontend.
- CI runs on pull requests and builds the Docker images after all checks pass.
- API system tests (`system-tests/`, JUnit + REST Assured against a real stack) and a Playwright end-to-end test of the login → project → task flow (US-05).
- Task title boundary tests (US-05).
- Test profile `st` (database `matriz_st`) and Keycloak client `matriz-system-tests` (local only).
- Stack tests (system + E2E) in CI and as optional gates of the release workflow.
- Test plan ([docs/test-plan.md](docs/test-plan.md)) and code and security review ([docs/quality-security-review.md](docs/quality-security-review.md)).
- Favicon and brand image.

### Changed

- nginx verifies HTTPS backend certificates and sends the upstream hostname for routing/SNI.
- `docker-compose.yml` renamed to `compose.yaml`; `docker compose up -d` still starts only PostgreSQL and Keycloak.
- Keycloak always issues tokens for `http://localhost:8180`, so the backend container can validate them while reaching Keycloak as `keycloak:8180`.
- The release workflow builds the backend image from `backend/Dockerfile`.
- Demo seed: the `admin` user is a member of all demo projects (dev only).

### Fixed

- Concurrent first-login requests and directory sync create the user projection atomically, avoiding duplicate-key failures on a fresh database without reactivating disabled users.

## [0.1.0] - 2026-11-11

First MVP. Tag `v0.0.1` points to the same commit.

### Added

- Login and logout through Keycloak (OIDC + PKCE); accounts managed in Keycloak and mirrored by the backend (US-01, US-02).
- Projects: create, list, search, view, edit, delete; member management with Manager and Member roles (US-03, US-04, US-14, US-15).
- Dashboard with the user's projects and task counts per status (US-15).
- Tasks: create, change status on a board, delete, filter by status and priority, progress per project (US-05 – US-09).
- Several assignees per task and comments (US-12, US-13).
- REST API documented in [openapi.yaml](openapi.yaml); hexagonal backend checked by ArchUnit.
- CI workflow and a manual release workflow publishing images to GitHub Container Registry.

[0.2.0]: https://github.com/PHGil/matriz/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/PHGil/matriz/releases/tag/v0.1.0
