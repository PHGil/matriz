# Matriz — Implementation Plan (Class 3)

> Status: **v0.2 — approved scope, implementation not started.** Each increment is implemented, verified and reviewed before the next one starts. Update the **Status** column as work progresses.

## 1. Scope reconciliation (Lab Sheet 03 vs approved MVP)

| #   | Lab Sheet 03 asks for                        | Approved MVP (Class 1/2)                      | Resolution (approved by the product owner)                                                                                                                                       |
| --- | -------------------------------------------- | --------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Several assignees per task                   | At most one assignee (FR-24)                  | **Adopt several assignees.** New `TASK_ASSIGNEE` table; FR-24 rewritten.                                                                                                         |
| 2   | Task comments                                | Out of scope (W)                              | **In scope** (FR-27). Author and date set by the backend.                                                                                                                        |
| 3   | Delete projects                              | Not specified (only archive, S)               | **In scope** (FR-15). Manager only; cascades to members, tasks, assignments and comments.                                                                                        |
| 4   | Project search by name; task filters         | FR-32 (S, post-MVP)                           | **In scope**: search projects by name (FR-16) and filter tasks by status and priority (FR-32 promoted to M).                                                                     |
| 5   | Home dashboard with counts per status        | Progress per project (FR-31)                  | **In scope** (FR-34): accessible projects with counts per status and link to detail.                                                                                             |
| 6   | Field rules from the Lab 02 example          | Name 1–100, title 1–200, priority optional    | **Adopt the lab values**: project name 3–100, description ≤ 1000; task title 3–150, description ≤ 2000, priority required (default `MEDIUM`); comment 1–2000.                    |
| 7   | Demo accounts by seed; registration optional | Accounts created by the Administrator (FR-03) | **Both**: demo accounts seeded in the Keycloak realm import; accounts managed in the Keycloak admin console (FR-03/04); members FR-12 implemented. **Lockout (NFR-04) removed.** |
| 8   | Pagination "if defined in the contract"      | Not defined                                   | **No pagination** (A-08: ≤ 500 tasks per project). Counts come from dedicated aggregates.                                                                                        |
| 9   | Layout adapts to different widths            | Desktop ≥ 1280 px (A-07, NFR-06)              | Fluid layout that stays usable on narrower windows; no mobile-specific design.                                                                                                   |
| 10  | Session lifecycle documented                 | ADR-05: opaque token in cookie                | **Replaced by Keycloak** (ADR-08, ADR-09): OIDC Authorization Code + PKCE, bearer tokens. Lifecycle documented in OpenAPI and README.                                            |

Out of scope still: self-registration, password recovery (D-05), "My tasks" (FR-33), archive (FR-14), change own password (FR-05), notifications, real-time updates.

## 2. Access rules (consolidated)

Non-members of a project receive **404** for the project and everything under it (EC-05). Members who lack the required role receive **403**.

| Action                                             | Administrator\*        | Manager | Member | Other user |
| -------------------------------------------------- | ---------------------- | ------- | ------ | ---------- |
| Create project                                     | ✓                      | ✓       | ✓      | ✓          |
| View project, tasks, members, comments, progress   | —                      | ✓       | ✓      | 404        |
| Edit / delete project                              | —                      | ✓       | 403    | 404        |
| Add / remove members                               | —                      | ✓       | 403    | 404        |
| Create / edit task, change status and priority     | —                      | ✓       | ✓      | 404        |
| Delete task                                        | —                      | ✓       | 403    | 404        |
| Add / remove assignees (eligible = active members) | —                      | ✓       | ✓      | 404        |
| Add / list comments                                | —                      | ✓       | ✓      | 404        |
| Create / deactivate / reactivate accounts          | Keycloak admin console | —       | —      | —          |

\* The Administrator has no access to project content by being admin (A-03); they only get the project rights from their own memberships (AA-01).

Identity (project manager, comment author, assignment date) is always taken from the validated access token, never from the request body.

## 3. Technical decisions for implementation

| ID     | Decision                                                                                                                                                                                                                                                                                                                                                               | Status    |
| ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- |
| IMP-01 | Repository layout: `backend/` (Quarkus, Maven), `frontend/` (Angular), `keycloak/` (realm import), `docker-compose.yml` (PostgreSQL + Keycloak), `.env.example`.                                                                                                                                                                                                       | Confirmed |
| IMP-02 | Java 17 + latest Quarkus 3.x LTS: `quarkus-rest`, `rest-jackson`, `hibernate-orm-panache`, `jdbc-postgresql`, `flyway`, `hibernate-validator`, `oidc`, `scheduler`, `keycloak-admin-rest-client`.                                                                                                                                                                      | Confirmed |
| IMP-03 | ~~bcrypt password hashing~~ — not needed: Keycloak owns passwords (ADR-08).                                                                                                                                                                                                                                                                                            | Removed   |
| IMP-04 | Keycloak 26.x, realm `matriz`: public client `matriz-frontend` (Authorization Code + PKCE S256, redirect `http://localhost:4200/*`); confidential client `matriz-backend` with service account role `view-users` (user sync); access token 5 min; SSO session idle 30 min / max 8 h; brute-force detection off; login pages in English (accepted exception to NFR-05). | Confirmed |
| IMP-05 | `quarkus-oidc` in service (bearer) mode validates the JWT; every resource requires authentication; a `CurrentUser` bean maps `sub`/`name`/`email` to `app_user` (creates it if missing); authorisation checks in services (`ProjectAccess`).                                                                                                                           | Confirmed |
| IMP-06 | Schema via Flyway migrations (`db/migration`). Demo data: users in the Keycloak realm import (fixed ids, fictitious passwords, imported only if the realm does not exist) + Flyway location `db/seed` (`dev` only) with projects/tasks for those ids. Never wipes data.                                                                                                | Confirmed |
| IMP-07 | Frontend: Angular 22 on Node.js 26 LTS (supported: `^26.0.0`); `keycloak-angular`/`keycloak-js` (login redirect, silent token refresh, logout); bearer token added only to API calls; router guards; 401 → login with "Sessão expirada"; API base URL in one config file; dev proxy `/api → localhost:8080`.                                                           | Confirmed |
| IMP-08 | Status changes from the board through a "Mover para" action (as in the wireframe menu); drag-and-drop is optional.                                                                                                                                                                                                                                                     | Confirmed |
| IMP-09 | Backend tests with `@QuarkusTest` + REST Assured against a PostgreSQL started by Quarkus Dev Services (Docker); users simulated with `@TestSecurity` + `@OidcSecurity` (`quarkus-test-security-oidc`), so tests do not need Keycloak.                                                                                                                                  | Confirmed |
| IMP-10 | Local development runs over HTTP (Keycloak in `start-dev`); HTTPS (NFR-02) is provided by the reverse proxy in deployment (D-04).                                                                                                                                                                                                                                      | Confirmed |
| IMP-11 | Initial password: the Administrator sets it in the Keycloak admin console (marked *temporary*, so the user changes it at first login) and communicates it out-of-band (relates to **D-05**).                                                                                                                                                                           | Confirmed |
| IMP-12 | ~~Lockout mitigation~~ — not applicable: lockout (NFR-04) removed by the product owner.                                                                                                                                                                                                                                                                                | Removed   |
| IMP-13 | User sync (ADR-09): scheduled job at startup and every 5 min reads users from the Keycloak Admin API (`matriz-backend` service account) and upserts `app_user` (name, email, active).                                                                                                                                                                                  | Confirmed |
| IMP-14 | Keycloak persists in the same PostgreSQL server, separate database `keycloak` (created by an init script); credentials only in `.env`.                                                                                                                                                                                                                                 | Confirmed |

## 4. Increments

| Inc | Name                         | Lab ex. | Status                           |
| --- | ---------------------------- | ------- | -------------------------------- |
| 0   | Scaffold and persistence     | 2       | Done                             |
| 1   | Authentication               | 3       | Not started                      |
| 2   | Projects, members, dashboard | 4       | Not started                      |
| 3   | Tasks, filters, progress     | 5       | Not started                      |
| 4   | Assignees and comments       | 6       | Not started                      |
| 5   | Account administration       | —       | Removed (Keycloak admin console) |
| 6   | Verification                 | 8       | Not started                      |
| 7   | Review and delivery          | 9       | Not started                      |

### Inc 0 — Scaffold and persistence

- **Components:** `backend/` Quarkus project; `frontend/` Angular project; `docker-compose.yml` (PostgreSQL + Keycloak); `keycloak/matriz-realm.json`; `.env.example`; `.gitignore`; Flyway `V1__schema.sql` (all tables in [database.md](database.md)); `db/seed/R__demo_data.sql`.
- **Depends on:** nothing.
- **Acceptance:** `docker compose up -d` starts PostgreSQL and Keycloak with the `matriz` realm and demo users; `./mvnw quarkus:dev` starts and applies migrations + seed; restarting does not re-seed or wipe data; `npm start` serves the Angular app; no real credentials in the repo.
- **Verification:** `psql` shows tables and demo rows; restart twice and count rows; Keycloak console shows the realm; `/q/health` returns UP.
- **Result (Inc 0):** Quarkus 3.40.1.1 LTS, Angular 22.2, PostgreSQL 18 (host port 5432), Keycloak 26.8.1. Realm imported with env placeholders resolved (service-account token obtained, 4 demo users listed). Flyway applied `V1` + `R__demo_data`; health UP; after adding a project and deleting a seeded task, a restart reported "No migration necessary" and kept both changes. Seed implemented as an idempotent **repeatable** migration instead of `V1000` (a high version number would block later schema migrations). `ng build` and `ng test` (2 tests) pass on Node 26.

### Inc 1 — Authentication (US-01)

- **Backend:** `auth` module — OIDC bearer validation, `CurrentUser` provisioning, `GET /auth/me`; `users` sync job (IMP-13); problem+json errors (401/403/404).
- **Frontend:** Keycloak login redirect, auth guard, token on API calls, 401 handling, header with user name and "Sair" (Keycloak logout).
- **Acceptance:** US-01 AC1, AC2, AC4, AC5; US-02 AC1, AC3 (via Keycloak console + sync).
- **Verification:** `@QuarkusTest`: request without token → 401, with simulated user → `/auth/me` returns and provisions the user; manual login/logout with demo accounts in the browser; disable a user in Keycloak and confirm loss of access within 5 min.

### Inc 2 — Projects, members, dashboard (US-03, US-04, FR-15, FR-16, FR-34)

- **Backend:** `projects` module — list (with `q` search, counts per status, `myRole`), create, get, update, delete (cascade); members list/add/remove (BR-04, BR-06); `GET /users?q=` (active users directory for adding members).
- **Frontend:** dashboard (projects + counts), projects list with search, create/edit form, delete with confirmation, members screen.
- **Acceptance:** US-03 AC1–AC3 (with 3–100 rule), US-04 AC1–AC5, delete leaves no orphans, first increment of the lab sheet (login → create → list → reload → restart backend → still there).
- **Verification:** tests for validation (name blank/2 chars), authorisation (other user gets 404, member gets 403 on edit), create + read back; manual end-to-end.

### Inc 3 — Tasks, filters, progress (US-05 – US-09)

- **Backend:** `tasks` module — list with `status`/`priority` filters, create, get, update, delete (Manager only), progress.
- **Frontend:** project detail (board with 3 columns, filters, create form, progress bar), task detail (edit, status, priority, delete with confirmation); lists and counts refresh after every operation without reload.
- **Acceptance:** US-05 – US-08 ACs (with 3–150 rule and default `MEDIUM`), US-09 filters, counts unaffected by filters.
- **Verification:** tests for title validation, member cannot delete (403), create + get; manual board flow.

### Inc 4 — Assignees and comments (FR-24, FR-27)

- **Backend:** `POST/DELETE /projects/{id}/tasks/{taskId}/assignees`; `GET/POST /projects/{id}/tasks/{taskId}/comments`.
- **Frontend:** in task detail — assignee picker (project members), remove assignment, comment list (author, date) and form.
- **Acceptance:** duplicate assignment refused (409) and never duplicated; non-member or deactivated user refused (400); removing a member removes their assignments; comment 1–2000 and not blank; author/date from backend; data still there after reopening.
- **Verification:** tests for duplicate assignment, comment validation, comment author = authenticated user.

### Inc 5 — Account administration (US-02) — removed

Covered by the Keycloak admin console (ADR-08) and the user sync (Inc 1). US-02 is verified in Inc 6.

### Inc 6 — Verification (Lab Ex. 8)

- Create `docs/verification.md` with cases 01–09 (expected, observed, evidence), automated test results, manual checks, limitations and any bug + regression test.

### Inc 7 — Review and delivery (Lab Ex. 9)

- AI-assisted diff review (permissions, validation, errors, scope); one refactoring if beneficial; update OpenAPI, docs, AGENTS.md (commands) and README (requirements, setup, seed, demo accounts, run, tests); three AI usage examples in `docs/verification.md`; clean-clone check.

## 5. Risks

| Risk                                                                    | Mitigation                                                     |
| ----------------------------------------------------------------------- | -------------------------------------------------------------- |
| Node.js 25 (currently installed) is not supported by Angular 22.        | Use Node.js 26 LTS (Angular 22 supports `^26.0.0`).            |
| Dev Services and Docker Compose both need Docker.                       | Docker is a documented prerequisite.                           |
| Scope grew beyond the original MVP (comments, deletion, N:M assignees). | Implemented last within each area; documented in requirements. |
| Deactivated user keeps API access until the access token expires.       | Token lifetime 5 min; documented in US-02 AC3 and S-06.        |
