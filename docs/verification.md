# Matriz — Verification (Lab Sheet 03, Exercise 8)

Results of the MVP verification. Automated tests and manual checks are kept apart; anything not run is listed under [Limitations](#limitations).

Environment: Linux, Java 17, Quarkus 3.40.1.1, Node 26, PostgreSQL 18 and Keycloak 26 in Docker Compose. Demo accounts from the Keycloak realm import (`ana`, `bruno`, `carla`).

## 1. Commands and results

| Command                                                                 | Result                         |
| ----------------------------------------------------------------------- | ------------------------------ |
| `cd backend && ./mvnw clean test` (PostgreSQL via Quarkus Dev Services) | 45 tests, 0 failures, 0 errors |
| `cd frontend && npx ng build`                                           | Build succeeds                 |
| `cd frontend && npx ng test --watch=false`                              | 5 tests pass                   |
| `curl -i localhost:8080/api/v1/projects` (no token)                     | `401`                          |
| `curl -i -H "Authorization: Bearer abc.def.ghi" .../api/v1/projects`    | `401`                          |

Backend tests by class: `AuthResourceTest` 2, `ProjectResourceTest` 7, `TaskResourceTest` 12, `TaskCollaborationResourceTest` 6, `UserServiceTest` 1, `ArchitectureTest` 5, `ProjectTest` 4, `ProgressTest` 3, `TaskTest` 5.

Lab minimum (three relevant automated tests):

| Kind                       | Test                                                                                                           |
| -------------------------- | -------------------------------------------------------------------------------------------------------------- |
| Validation rule            | `TaskResourceTest.invalidTaskDataIsRejected`, `ProjectResourceTest.invalidNamesAreRejectedWithFieldErrors`     |
| Authorisation scenario     | `ProjectResourceTest.outsiderCannotSeeOrChangeProjectEvenWithItsId`, `TaskResourceTest.memberCannotDeleteTask` |
| Create + read back from DB | `TaskResourceTest.memberCreatesTaskAndReadsItBack` (API and a separate-transaction SQL query)                  |

## 2. Cases

| Case | Scenario             | Expected                                                                                   | Observed                                                                                                                                                                                                                                                                         | Evidence                                                                                                                                                                                                                                        |
| ---- | -------------------- | ------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01   | Access to the app    | Valid credentials enter; invalid and unauthenticated requests are handled.                 | Matches. Wrong password → "Invalid username or password."; valid login shows the user name; no token and malformed token → 401; disabled account refused.                                                                                                                        | Auto: `AuthResourceTest` (2). Manual: Inc 1 notes in the plan (login, wrong password, disabled user) and `curl` 401 results above; Bruno login in browser.                                                                                      |
| 02   | Project management   | Create, search, edit and delete follow the permissions.                                    | Matches for create, list, search, delete, member add; outsider gets 404 and member 403.                                                                                                                                                                                          | Auto: `ProjectResourceTest` (7). Manual: Inc 2 notes in the plan.                                                                                                                                                                               |
| 03   | Task persistence     | The task appears without reload and survives a backend restart.                            | Creating "Validar acessibilidade" showed it in "Por fazer" immediately; the count went 2 → 3 and progress 40% → 33% with no reload; the DB row exists. After stopping and restarting the backend and the containers, seeded tasks, an assignment and a comment were still there. | Auto: `memberCreatesTaskAndReadsItBack`. Manual: browser + `psql` (`TODO` 3, `IN_PROGRESS` 1, `DONE` 2).                                                                                                                                        |
| 04   | Data validation      | Empty, whitespace-only and invalid values are rejected.                                    | Matches. Whitespace comment → "O comentário é obrigatório." and nothing saved; API returns 400 problem+json with field errors for projects, tasks, patches and comments.                                                                                                         | Auto: `invalidNamesAreRejectedWithFieldErrors`, `invalidTaskDataIsRejected`, `invalidTaskChangesAreRejected`, `invalidCommentsAreRejected`, `TaskTest`, `ProjectTest`. Manual: comment form.                                                    |
| 05   | Status and priority  | Edits update the task, the filters and the counts coherently.                              | API level matches (counts, rounding, filters not affecting counts). In the browser, counts changed after creating a task; status change from the UI was not exercised in this session.                                                                                           | Auto: `memberChangesStatusAndEditsTask`, `progressReflectsStatusChanges`, `filtersByStatusAndPriorityWithoutAffectingProgress`, `ProgressTest`.                                                                                                 |
| 06   | Assignees            | Assign and remove work; the same assignment is never duplicated.                           | Matches. Two assignees listed after reopening; one removed; duplicate → 409 and a single `task_assignee` row; non-member/unknown/missing user → 400. In the browser the picker offers only unassigned active members; assign and remove both update the list.                    | Auto: `TaskCollaborationResourceTest` (assign/remove, duplicate, non-member). Manual: browser (Ana assigned, then removed).                                                                                                                     |
| 07   | Comments             | Content, authenticated author and date are saved and visible.                              | Matches. The author comes from the token (an extra `authorId` in the body is rejected with 400); the comment shows "Bruno Costa · 11/11/26, 12:33" and was still listed after the backend restart.                                                                               | Auto: `commentAuthorAndDateComeFromTheServer`. Manual: browser.                                                                                                                                                                                 |
| 08   | Authorisation in API | Another user cannot read or change resources, even by changing ids.                        | Matches. Outsider gets 404 on project, tasks, progress, assignees and comments; a task requested through another project id → 404; member gets 403 on edit/delete.                                                                                                               | Auto: `outsiderCannotSeeOrChangeProjectEvenWithItsId`, `outsiderCannotReadOrCreateTasks`, `outsiderCannotAssignOrComment`, `taskIsNotFoundThroughAnotherProjectId`, `memberCannotDeleteTask`. Manual: Inc 2 (Carla by URL → "Não encontrado."). |
| 09   | Deletion and errors  | Deleting respects dependencies; API errors give no success message and keep the form data. | Matches. With PostgreSQL stopped, posting a comment showed "Ocorreu um erro no servidor. Tente novamente.", no success message, and the typed text stayed in the field; after PostgreSQL returned, the same click saved it. Deleting a project/task removes dependents.          | Auto: `deletingProjectRemovesDependentRows`, `managerDeletesTaskAndCountsFollow`. Manual: browser with `docker compose stop postgres`.                                                                                                          |

## 3. Bugs found and regression tests

| Bug                                                                              | Cause                                                           | Fix                                 | Regression test                                                                |
| -------------------------------------------------------------------------------- | --------------------------------------------------------------- | ----------------------------------- | ------------------------------------------------------------------------------ |
| Malformed JSON bodies returned an empty 400 (Inc 2).                             | Jackson errors were not mapped to problem+json.                 | Added a mapper in `ProblemMappers`. | `ProjectResourceTest.invalidNamesAreRejectedWithFieldErrors` (malformed JSON). |
| Project `PATCH` with only `name` erased the description (commit `1338c02`).      | Omitted fields were treated as `null`.                          | Omitted fields are kept.            | `ProjectResourceTest.patchWithOnlyNameKeepsDescription`.                       |
| Malformed token triggered Keycloak token introspection and an ERROR log (Inc 1). | Opaque-token introspection was enabled without a client secret. | Introspection disabled.             | No automated test; verified with `curl` (401, no ERROR). Known gap.            |

## 4. Examples of AI use

These entries were written by the assistant from the project record. The developer should confirm or edit the "decision" wording.

1. **Scope reconciliation.** Goal: compare Lab Sheet 03 with the approved MVP and list gaps. Decision: ten differences resolved by the product owner (several assignees, comments, project delete, filters, lab field limits, no pagination, Keycloak instead of local sessions…), recorded in [implementation-plan.md](implementation-plan.md#1-scope-reconciliation-lab-sheet-03-vs-approved-mvp). Verification: the resulting limits (3–100, 3–150, ≤ 2000) are asserted in the validation tests.
2. **Hexagonal refactor.** Goal: restructure the backend into domain / application ports / adapters. Decision: accepted (ADR-10, commit `b150937`). Verification: `ArchitectureTest` (5 rules) plus the existing API tests passing unchanged.
3. **Corrected suggestion (assignee picker).** The first draft disabled the "Atribuir" button with `!picker.value`, a template reference that Angular does not track reactively, so the button state could go stale. Decision: replaced by a check on the `eligible()` signal plus a guard in the click handler. Verification: browser run (assign Ana, picker updated; remove Ana, she returned to the picker).

## Lab Sheet 04 — test levels and reviews

Unit, system (integration) and end-to-end tests were added after the verification above; details, commands and results are in [test-plan.md](test-plan.md) and [quality-security-review.md](quality-security-review.md). Latest runs (2026-11-11): backend `./mvnw clean test` 51 tests pass (was 45; six new `TaskTest` cases); `system-tests` 30 tests pass against the `st` backend (run twice); Playwright 2 tests pass (repeated 3 times); `prettier --check` and `ng test` (5) pass. Two review findings are open for the author's decision (F-01, F-02).

## Render + Neon preparation — 2026-11-19

Approved scope: three Render free container services and separate Neon databases (D-04), preserving the existing local stack. Cloud resources were **not** created; no commits were pushed. Setup, provider limits and the remaining public-deployment checklist are in [deployment.md](deployment.md).

| Check | Result |
| --- | --- |
| `cd backend && ./mvnw -B -q test` | **55 passed**, 0 failures/errors, PostgreSQL 18 via Dev Services. Includes six simultaneous first-login requests, preserving a disabled projection, updating a disabled user's profile without reactivation, and rejecting a different identity with the same email. |
| Frontend, Node 26: `npx prettier --check .`, `npx ng build`, `npx ng test --watch=false` | Formatting/build pass; **5 tests passed**. |
| `node --test keycloak/production-realm.test.mjs` (also through CI's Node 26 container command) | **3 passed**: no human/demo accounts or password-grant client in production; exact origin placeholders/PKCE; approved session rules and local realm preserved. |
| `docker build` for backend, frontend and production Keycloak | All three images build successfully. |
| `docker compose --env-file .env.example --profile app config -q` | Pass; local Compose configuration still valid. |
| Backend without `DB_JDBC_URL` | A separate container using only `DB_HOST`, `DB_PORT` and `DB_NAME` reaches readiness `UP`; original URL fallback preserved. |
| `render.yaml` against Render's official JSON schema | Pass, using Python `jsonschema`; all three services explicitly `plan: free`. This does not replace account-side Blueprint validation or deployment. |
| `npx --offline --yes @redocly/cli@2.72.0 lint openapi.yaml` | Pass with two existing warnings: missing license metadata and the local-development server URL. |
| Frontend runtime configuration | Local default accepted; trailing-slash, non-HTTP(S) and multiline origins rejected with startup failure. `/config.js` contains configured origin and `Cache-Control: no-store`; SPA deep links work. |
| Isolated production-image smoke, fresh PostgreSQL databases, local TLS proxy | Keycloak uses production startup, public-style HTTPS hostname and imported realm. Discovery issuer/authorization endpoint use HTTPS. Quarkus readiness returns `UP`; frontend proxies to HTTPS backend with certificate verification; unauthenticated `/api/v1/projects` returns `401`. Both Keycloak and application JDBC connections confirmed TLS in `pg_stat_ssl`, using `sslmode=verify-full` and Java trust. |
| Chromium through that isolated HTTPS stack | New account logs in with Authorization Code + PKCE; `/auth/me` returns `200`; creates a project and task, task appears in "Por fazer", logs out to Keycloak. Repeated login after restarting Keycloak/backend finds the same project/task and can log out again. |
| Java container memory limits | Both ran with Docker `--memory=512m`; observed after the final small browser smoke: Keycloak **456.2 MiB**, backend **221.9 MiB**. This is not a load test or a Render resource/performance guarantee. |
| Cleanup | Isolated smoke containers/network and generated certificate/private-key/truststore files removed; existing development services/data not used. |

**Issue found and fixed (US-01, ADR-09).** A completely new user triggered concurrent dashboard and `/auth/me` requests before directory sync. Both tried to insert the same projection, causing a duplicate-key `500` and missing user name. Human approval expanded the deployment work to fix it: both login and sync now use atomic insert-if-absent, return the persisted identity and never overwrite an existing projection with a newly constructed active user. Conflicts for an email belonging to another identity remain explicit (`409`); no account merging. Backend regressions and the fresh-database browser flow above pass.

The TLS smoke used generated local certificates trusted explicitly by Java/nginx/curl. Chromium ignored the test certificate's trust error only in that isolated test context; production images do not disable certificate verification. The local simulation validates wiring, not Render's certificates, 0.1-CPU startup timing, public-network behaviour or Neon's real endpoints.

**Still pending after the preparation step** (resolved partly below): actual Render/Neon setup and public checks.

## Render + Neon deployment — 2026-11-19

Commits `c7544c9` and `f1e3e77` pushed to `master` (approved); GitHub Actions run for `f1e3e77`: **success**. Resources created through the providers' APIs: Neon Free project `matriz` (PostgreSQL 18, `aws-eu-central-1`, direct endpoint) with databases `matriz`/`keycloak` owned by separate roles `matriz_app`/`matriz_keycloak`; three Render Free Docker web services in Frankfurt with `autoDeployTrigger: checksPass`. Render added suffixes to two hostnames, so `BACKEND_URL` was corrected and the frontend redeployed. Credentials were set only as provider environment variables and in a private file outside the repository.

| Public URL | Service |
| --- | --- |
| https://matriz-frontend-gpxo.onrender.com | Frontend (application entry point) |
| https://matriz-backend-kayx.onrender.com | Backend API |
| https://matriz-keycloak.onrender.com | Keycloak |

| Check | Result |
| --- | --- |
| All three Render deploys of `f1e3e77` | `live`. |
| Keycloak discovery | Issuer `https://matriz-keycloak.onrender.com/realms/matriz`. |
| Backend `/q/health/ready` | `UP`, database check `UP` (Neon over TLS, `sslmode=verify-full`). |
| Frontend `/config.js` and `/` | Keycloak origin is the public HTTPS URL; `/` returns `200`. |
| Unauthenticated `/api/v1/projects` through the frontend | `401`. |
| `http://` frontend | `301` to the HTTPS URL. |
| Presentation accounts | Fictitious `ana`, `bruno`, `carla` created in realm `matriz` through the admin API with unique random passwords (not in the repository). |
| Chromium against the public URLs | `ana` logs in with Authorization Code + PKCE; `/auth/me` and `/api/v1/projects` return `200`; Projetos page loads; **Sair** returns to the Keycloak login. No test data was created. |

**Still pending:** two-user membership checks on the public deployment, data persistence after a cloud redeploy, sustained memory/load and free-quota behaviour. Keycloak still uses the temporary bootstrap admin account. No full system/E2E suite is run against the cloud (they truncate data).

## Limitations

- Not run: GitHub Actions pipeline and the OpenAPI lint step; start-up of the app from a clean clone (`.env`, `docker compose`, `quarkus:dev`, `npm start`). A clean `git clone` did pass `./mvnw clean test` (45), `npm ci`, `prettier --check`, `ng build` and `ng test` (5); the 8 h inactivity logout and token expiry while editing (EC-07) were not exercised either.
- Not exercised in the browser in this session: changing status from the board/detail, editing a project, removing a member, delete confirmation dialogs, and the layout on narrow widths. These are covered only by API tests (or earlier manual checks noted in the plan).
- The demo database was modified by the manual checks (one task and one comment added to the seeded data; one assignment removed). `docker compose down -v` restores the seed.
- Backend tests need Docker (Dev Services). `ng test` covers only a few UI helpers; there are no component tests for the new assignee and comment components.
