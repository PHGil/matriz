# Matriz 0.2.0 — release candidate notes

Status: **candidate**, not published. Proposed tag `v0.2.0` (Semantic Versioning: new backward-compatible capabilities over `v0.1.0`, no API or schema change). Changes in [CHANGELOG.md](../CHANGELOG.md#020---unreleased-release-candidate).

## Highlights

- **Run the whole app with one command.** `docker compose --profile app up -d --build` builds and starts PostgreSQL, Keycloak, backend and frontend; open <http://localhost:4200>. The images build from source, so no local Java or Node is needed for this mode.
- **Safer pipeline.** CI runs on pushes to `master` and on pull requests; Docker images are built only after the backend, frontend, stack and OpenAPI checks pass.
- **More tests.** API system tests against a real stack and a Playwright end-to-end test of the main flow, also run in CI.
- **Cloud deployment preparation.** Render Blueprint, production Keycloak image and Neon TLS instructions in [deployment.md](deployment.md). Deployed at https://matriz-frontend-gpxo.onrender.com.

No new user-facing features, endpoints or database migration. Concurrent first-login requests now create one user projection instead of failing; an email owned by a different identity is reported explicitly as a conflict ([openapi.yaml](../openapi.yaml)).

## Upgrade notes

- `docker-compose.yml` is now `compose.yaml`. Plain `docker compose up -d` still starts only PostgreSQL and Keycloak for development.
- Keycloak now has `KC_HOSTNAME=http://localhost:8180`; recreate it with `docker compose up -d keycloak`. Existing data is kept.
- Backend adds optional `DB_JDBC_URL`; existing `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `KEYCLOAK_URL`, `MATRIZ_BACKEND_CLIENT_SECRET` remain supported. Frontend adds runtime `KEYCLOAK_URL` alongside `BACKEND_URL`; local defaults are unchanged.

## Verification

| Check                                                                       | Result (2026-11-16)                                                                                                                                          |
| --------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Backend unit + API tests, system tests, E2E (×3), frontend tests and format | 51, 30, 6 and 5 passed; formatting clean ([test-plan.md](test-plan.md#4-results-real-executions-2026-11-11-linux-java-17-node-26)), at commit `f99f7f9`      |
| `docker compose config -q`, `docker compose --profile app build`            | OK; images `matriz-backend` 451 MB, `matriz-frontend` 63 MB                                                                                                  |
| `docker compose --profile app up -d --wait`                                 | All four services `healthy`                                                                                                                                  |
| Main flow on the containers                                                 | Login as `ana` in the browser (Keycloak, PKCE) → dashboard with 3 projects; API through nginx with a real token: `/auth/me`, `/projects`, tasks answer `200` |
| Data after `docker compose --profile app down` + `up`                       | Same projects returned (volume kept)                                                                                                                         |
| Logs                                                                        | No errors; Keycloak warnings only (deprecated default features, "Full scope allowed" on two clients)                                                         |
| GitHub Actions for the new CI job (Docker build, pull requests)             | Not run yet (commits not pushed)                                                                                                                             |

Deployment-preparation verification (2026-11-19), including the first-login fix, three image builds, HTTPS/TLS simulation and remaining cloud checks: [verification.md](verification.md#render--neon-preparation--2026-11-19).

## Known limitations

- Public two-user checks, persistence after a cloud redeploy and Render memory/quota behaviour still need verification. See [deployment.md](deployment.md).
- Local Compose intentionally uses `start-dev` and the local realm with demo accounts and the test-only client; only the separate `keycloak/Dockerfile` is intended for Render.
- Not automated: concurrent edits, token expiry while editing, narrow screens, browsers other than Chromium (see [test-plan.md](test-plan.md#5-decisions-and-limits)).
