# Matriz — Test plan (Lab Sheet 04)

Scope: tasks (US-05, US-06, US-08) and the access rules around them (US-03, US-04, EC-05), plus the login → project → task path in the UI. Scenarios come from the acceptance criteria in [user-stories.md](user-stories.md) and the contract in [openapi.yaml](../openapi.yaml), not from the current behaviour of the code.

## 1. Approach

| Level       | Tool and location                                                                                                        | What it proves                                                                               |
| ----------- | ------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------- |
| Unit        | JUnit 5, `backend/src/test/java/com/matriz/domain/task/TaskTest`                                                         | Domain rules without Quarkus or a database (title limits, defaults, status changes).         |
| Integration | JUnit 5 + REST Assured, [system-tests/](../system-tests/README.md), against the running backend, PostgreSQL and Keycloak | HTTP codes, returned fields, persistence in the database, access control between demo users. |
| End-to-end  | Playwright, `frontend/e2e`                                                                                               | The user path through the real UI, with a real Keycloak login.                               |

Existing backend tests (`@QuarkusTest`, see [verification.md](verification.md)) stay as they are; the new system tests add black-box checks of the deployed stack.

**Isolation.** System and E2E tests use their own database `matriz_st` (backend profile `st`, no demo data). Each test truncates the application tables, registers the demo users again and builds its own data through the API, so no test depends on another or on a previous run. Credentials come from `.env` through the environment.

**Demo users.** `ana` (Manager of the test project), `bruno` (Member), `carla` (not a member). Tokens are real Keycloak access tokens (client `matriz-system-tests`).

## 2. Review of the assistant's proposal

The scenario matrix was drafted by the AI assistant from the user stories and then checked against the code and the documentation. Corrections made to its first assumptions:

| First assumption                         | Correction (source)                                                                                                        |
| ---------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| A non-member gets `403 Forbidden`.       | Non-members get `404`, so the project's existence is not revealed (EC-05); only a member without the role gets `403`.      |
| Title length is checked on the raw text. | The title is trimmed first, so `"   ab   "` is invalid and `"  abc  "` is valid (US-05 AC2, `TaskTest`).                   |
| A due date in the past is an error.      | It is accepted (US-05 AC4, EC-09).                                                                                         |
| The UI can use `data-test` attributes.   | The app has none and production code is not changed; the E2E uses roles, labels and the `aria-label` of the board columns. |
| The E2E can start already authenticated. | The flow must include the login, so the test signs in through the Keycloak form (US-01).                                   |
| Tests can use the seeded demo data.      | Seeds are dev-only and mutable; each test creates its data in an emptied `matriz_st`.                                      |

These are the assistant's corrections. The author still has to confirm the choices (Section 5).

## 3. Scenarios, expected results and tests

Essential scenarios required by the lab sheet are marked **E**.

### Unit — `TaskTest`

| ID    | Scenario (input)                                                      | Expected                                        | Test                                             |
| ----- | --------------------------------------------------------------------- | ----------------------------------------------- | ------------------------------------------------ |
| TP-01 | Valid title `"  Rever proposta  "`, no other fields                   | Title trimmed, status `TODO`, priority `MEDIUM` | `newTaskStartsAsTodoWithMediumPriority`          |
| TP-02 | **E** Invalid title: blank, 2 and 151 characters; description of 2001 | `ValidationException`                           | `invalidTitleOrDescriptionIsRejected`            |
| TP-03 | **E** Boundary: titles of 3 and 150 characters                        | Accepted                                        | `titleOnTheLengthLimitsIsAccepted`               |
| TP-04 | Boundary: titles of 0, 2 and 151 characters                           | Rejected                                        | `titleJustOutsideTheLengthLimitsIsRejected`      |
| TP-05 | Length is measured after trimming                                     | `"   abc   "` accepted, `"   ab   "` rejected   | `titleLengthIsMeasuredAfterTrimming`             |
| TP-06 | Due date in the past                                                  | Accepted                                        | `pastDueDateIsAccepted`                          |
| TP-07 | Any status change, including reopening                                | Allowed                                         | `anyStatusTransitionIsAllowedIncludingReopening` |

### Integration — `system-tests`

| ID    | Scenario (input)                                                                         | Expected                                                                                                                   | Test                                                                 |
| ----- | ---------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| TP-10 | **E** Bruno (member) creates a task with only `"  Review proposal  "`, then Ana reads it | `201`, `Location`, trimmed title, `TODO`, `MEDIUM`, no assignees; `GET` by id and list return it; the row exists in `task` | `CreateTaskTest.testToCreateTaskAndReadItBack`                       |
| TP-11 | **E** Title with 0 or 2 or 151 characters, or only spaces                                | `400` `application/problem+json` with an error on field `title`; no row saved                                              | `testToCreateTaskWithInvalidTitle`, `testToCreateTaskWithBlankTitle` |
| TP-12 | Title with exactly 3 and 150 characters                                                  | `201`, one row saved                                                                                                       | `testToCreateTaskWithTitleOnTheLimits`                               |
| TP-13 | Description of 2001 characters                                                           | `400`, error on `description`, nothing saved                                                                               | `testToCreateTaskWithTooLongDescription`                             |
| TP-14 | Due date `2020-01-01`                                                                    | `201` with the same `dueDate` (EC-09)                                                                                      | `testToCreateTaskWithPastDueDate`                                    |
| TP-15 | Title `<script>alert(1)</script>`                                                        | `201`; stored and returned unchanged as text (EC-08)                                                                       | `testToCreateTaskWithHtmlTitle`                                      |
| TP-16 | Unknown task id                                                                          | `404` `application/problem+json`                                                                                           | `GetTaskTest.testToGetUnknownTask`                                   |
| TP-17 | Task of project A requested through project B (same user in both)                        | `404`                                                                                                                      | `testToGetTaskThroughAnotherProject`                                 |
| TP-18 | List tasks of project A when project B has tasks too                                     | Only A's tasks                                                                                                             | `testToListTasksOfOneProject`                                        |
| TP-19 | Member added after the task was created reads it                                         | `200`                                                                                                                      | `testToGetTaskAsLaterMember`                                         |
| TP-20 | **E** Carla (not a member) lists and reads Ana's tasks                                   | `404`, nothing revealed (EC-05)                                                                                            | `TaskAuthorizationTest.testNonMemberCannotReadTasks`                 |
| TP-21 | **E** Carla creates a task in Ana's project                                              | `404`; task count unchanged                                                                                                | `testNonMemberCannotCreateTask`                                      |
| TP-22 | **E** Carla changes the status of Ana's task                                             | `404`; status still `TODO` in the database (US-06 AC3)                                                                     | `testNonMemberCannotChangeStatus`                                    |
| TP-23 | **E** Carla deletes Ana's task                                                           | `404`; row still exists (US-08 AC3)                                                                                        | `testNonMemberCannotDeleteTask`                                      |
| TP-24 | IDOR: Carla uses Ana's task id under her own project id (GET, PATCH, DELETE)             | `404`; task unchanged                                                                                                      | `testTaskIsNotReachableThroughForeignProjectId`                      |
| TP-25 | Bruno (member) moves a task to `IN_PROGRESS`                                             | `200`; database updated (US-06 AC1)                                                                                        | `testMemberCanChangeStatus`                                          |
| TP-26 | Bruno (member, not Manager) deletes a task                                               | `403`; row still exists (US-08 AC3)                                                                                        | `testMemberCannotDeleteTask`                                         |
| TP-27 | Ana (Manager) deletes a task                                                             | `204`; row removed (US-08 AC1)                                                                                             | `testManagerCanDeleteTask`                                           |
| TP-28 | No token, and a malformed token                                                          | `401` both (US-01)                                                                                                         | `testRequestsWithoutValidTokenAreRefused`                            |
| TP-29 | Carla lists projects, gets, reads progress and members of Ana's project                  | Project not listed; `404` on the others (US-03 AC3)                                                                        | `ProjectAuthorizationTest.testNonMemberCannotSeeProject`             |
| TP-30 | Carla edits and deletes Ana's project                                                    | `404`; name and row unchanged                                                                                              | `testNonMemberCannotChangeProject`                                   |
| TP-31 | Carla adds herself / removes Bruno as member                                             | `404`; members unchanged (US-04 AC5)                                                                                       | `testNonMemberCannotManageMembers`                                   |
| TP-32 | Carla reads/posts comments and assigns herself on Ana's task                             | `404`; no comment and no assignee created                                                                                  | `testNonMemberCannotCommentOrAssign`                                 |
| TP-33 | Bruno (member) edits/deletes the project, adds/removes members                           | `403`; name and members unchanged (US-04 AC5)                                                                              | `testMemberCannotManageProject`                                      |
| TP-34 | Bruno comments on a task                                                                 | `201`; row saved (US-13)                                                                                                   | `testMemberCanComment`                                               |
| TP-35 | Ana adds Carla, then removes her                                                         | Carla gets `200`, then `404` (US-04 AC1, AC3)                                                                              | `testAccessFollowsMembership`                                        |

The existing `@QuarkusTest` classes (`TaskResourceTest`, `ProjectResourceTest`, `TaskCollaborationResourceTest`) already cover the same rules with a mocked identity; they were not duplicated beyond the cases above.

### End-to-end — `frontend/e2e/tests/tasks/create-task.spec.ts`

| ID    | Scenario                                                                                                    | Expected                                                                                                                    | Test                                                                          |
| ----- | ----------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| TP-40 | **E** Ana signs in through Keycloak → opens "Website 2027" from the list → creates "Validar acessibilidade" | Confirmation `Tarefa "Validar acessibilidade" criada.`; the task is a link in the "Por fazer" column and the column shows 1 | `should log in, open a project, create a task and see it in the To Do column` |
| TP-41 | **E** Title `ab` in the task form                                                                           | Field error "O título deve ter entre 3 e 150 caracteres."; "Por fazer" stays at 0                                           | `should not create a task with a too short title`                             |

No fixed waits are used: every step waits on an assertion (`expect(...).toBeVisible()` / `toContainText`).

## 4. Results (real executions, 2026-11-11, Linux, Java 17, Node 26)

| Command                                                                      | Result                                                                               |
| ---------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| `cd backend && ./mvnw -s ~/.m2/personal_settings.xml clean test`             | 51 tests, 0 failures, 0 errors (`TaskTest` 11: the 6 new cases are TP-03 to TP-05)   |
| `cd backend && ... -f ../system-tests/pom.xml verify` (backend profile `st`) | 30 tests, 0 failures, 0 errors; run twice in a row with the same result (repeatable) |
| `cd frontend && npx playwright test`                                         | 2 passed; `--repeat-each=3` gave 6 passed                                            |
| `cd frontend && npx prettier --check .` and `npx ng test --watch=false`      | Formatting clean; 5 tests pass (the e2e specs are not picked up by `ng test`)        |

Failures found while building the tests were problems of the tests or of the environment, not of the application:

| Symptom                                                           | Classification                       | Resolution                                                                                                                                                     |
| ----------------------------------------------------------------- | ------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| REST Assured could not serialise request bodies / missing logging | Test setup                           | Added `jackson-databind` and `commons-logging-jboss-logging` to `system-tests/pom.xml`.                                                                        |
| Playwright could not find the Keycloak field "Username or email"  | Test (wrong assumption on the label) | The realm uses the label "Username"; locator fixed. No application change.                                                                                     |
| Port 8080 already used by the dev backend                         | Environment                          | System tests first ran against a second `st` backend on 8081 (`MATRIZ_API_URL`); the E2E needs 8080, so the dev backend was stopped with the author's consent. |

No test was changed to hide an application defect, and no production rule was changed to obtain green tests.

## 5. Decisions and limits

- **Author to confirm:** the 404-for-non-members rule is taken from EC-05; the corrections in Section 2 were made by the assistant.
- **Not automated:** concurrent edits (EC-01), token expiry while editing (EC-07), the status change from the board in the UI, member removal in the UI, layout on narrow screens, and a second browser.
- **Mutation check not done:** the tests were not run against deliberately broken code, so their sensitivity to defects is shown by the assertions only.
- **CI:** the system and E2E tests run in the `stack-tests` job of `ci.yml` and are optional inputs of `release.yml`. The workflows were only checked for YAML syntax and by running their commands locally (packaged backend with profile `st` + system tests); they have **not** run on GitHub Actions yet.
- **Browsers:** only Chromium.
- **Environment coupling:** the E2E `resetDB` and the Java `DatabaseHelper` both list the application tables; a new table must be added to both.
