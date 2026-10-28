# Matriz — Constraints, Assumptions and Pending Decisions

> This document separates what is **confirmed** from what is **assumed**. Every assumption must be validated with the client before being treated as a final requirement.

## 1. Known constraints

| ID   | Constraint                                                                                                                                                   | Source           |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------- |
| C-01 | The application is called **Matriz**.                                                                                                                        | Client           |
| C-02 | It must support organising projects, managing tasks and tracking progress.                                                                                   | Client           |
| C-03 | It is meant to be used by teams (several users collaborating).                                                                                               | Client           |
| C-04 | In Lab Sheet 01 no code is implemented; the focus is the specification. Implementation starts in Class 3 ([implementation-plan.md](implementation-plan.md)). | Academic context |
| C-05 | Language, framework, database and architecture will only be chosen in the next phase (Class 2). Done: see [architecture.md](architecture.md).                | Academic context |

## 2. Assumptions

| ID   | Assumption                                                                                                                              | Impact if wrong                                                                     |
| ---- | --------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| A-01 | The application serves **a single organisation** (not multi-tenant).                                                                    | Data model and permissions would need isolation between organisations.              |
| A-02 | It is an **internal** tool: accounts are created by the Administrator, no self-registration.                                            | A sign-up flow, email verification and abuse protection would be needed.            |
| A-03 | The Administrator manages accounts but does **not** automatically see project content.                                                  | If the client requires global oversight, a profile with full read access is needed. |
| A-04 | Each project is visible only to its members.                                                                                            | If projects are public within the organisation, permissions become simpler.         |
| A-05 | There are two roles per project: **Manager** and **Member**. The project creator is the Manager. There is only one Manager per project. | More roles (e.g. viewer, several managers) change permissions and user stories.     |
| A-06 | Task statuses are fixed: To Do, In Progress, Done.                                                                                      | Custom workflows would require per-project configuration.                           |
| A-07 | The application is **web-based** and used mostly on desktop.                                                                            | Mobile support would require responsive design or a native app.                     |
| A-08 | Expected volume: up to ~100 users and up to 500 tasks per project.                                                                      | Affects performance requirements (NFR-07) and architecture choices.                 |
| A-09 | A project's progress is measured as the percentage of tasks in "Done".                                                                  | If the client wants to measure by effort, dates or milestones, the metric changes.  |
| A-10 | There are no sector-specific legal requirements beyond the GDPR.                                                                        | Regulated sectors may impose auditing, retention or specific hosting.               |

## 3. Security and privacy requirements

| ID   | Requirement                                                                                                                                                 |
| ---- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| S-01 | Passwords stored only with a strong salted hashing algorithm (NFR-01). Handled by Keycloak (ADR-08).                                                        |
| S-02 | Communication always encrypted via HTTPS/TLS (NFR-02).                                                                                                      |
| S-03 | Authorisation checked on the server for every request; users never access projects they are not members of (NFR-03, FR-13).                                 |
| S-04 | ~~Protection against brute-force login attempts (NFR-04).~~ **Removed in Class 3** (product owner decision). Can be re-enabled as a Keycloak realm setting. |
| S-05 | Login error messages do not reveal whether the email exists. (Keycloak default behaviour.)                                                                  |
| S-06 | Deactivated accounts cannot authenticate; access ends within the access-token lifetime (5 min, ADR-08).                                                     |
| S-07 | Personal data collected is limited to the minimum: name, email and password (GDPR — data minimisation).                                                     |
| S-08 | Credentials, keys and secrets are never stored in the repository or written to logs.                                                                        |
| S-09 | All user input is validated on the server and displayed content is escaped (injection and XSS prevention).                                                  |

## 4. Pending technical decisions

| ID   | Decision                                                              | When                                                                                                                                                                                         |
| ---- | --------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| D-01 | Language, frontend and backend frameworks.                            | **Decided (Class 2):** Java + Quarkus, Angular — [architecture.md](architecture.md) ADR-02                                                                                                   |
| D-02 | Database and data model.                                              | **Decided (Class 2):** PostgreSQL — [database.md](database.md)                                                                                                                               |
| D-03 | Architecture (monolith, API + SPA, etc.) and API design.              | **Decided (Class 2):** modular monolith, REST API + SPA — [architecture.md](architecture.md), [openapi.yaml](../openapi.yaml)                                                                |
| D-04 | Hosting and deployment infrastructure.                                | TBD                                                                                                                                                                                          |
| D-05 | Password recovery mechanism (email vs. reset by the Administrator).   | To validate with the client. MVP: Administrator sets the initial password in the Keycloak admin console (IMP-11, confirmed by the product owner). Keycloak can later provide email recovery. |
| D-06 | Need for SSO or two-factor authentication.                            | To validate with the client. Keycloak (ADR-08) supports both through configuration.                                                                                                          |
| D-07 | Data retention policy for deactivated accounts and archived projects. | To validate with the client                                                                                                                                                                  |
| D-08 | Behaviour when deleting a user (vs. only deactivating).               | To validate with the client                                                                                                                                                                  |

## 5. Scope limitations

- The MVP covers only what is described in [requirements.md](requirements.md#5-mvp-scope); everything under "Out of initial scope" must not be implemented without approval.
- There are no notifications: users only see changes when they open the application.
- No real-time collaboration: updates made by other users appear after reloading the view.
- User interface in Portuguese (Portugal) only.
- No data import/export.

## 6. Open questions for the client

1. How many users and teams will use the application? (A-08)
2. Is the application for internal use only, or also for external clients/partners? (A-02)
3. Should projects be private per team or visible to the whole organisation? (A-04)
4. How do you want to measure "work progress"? (A-09)
5. Which task statuses do you use today? Do you need to customise them? (A-06)
6. Do you currently use any tool? What doesn't work well in it?
7. Is there a corporate authentication system (e.g. Microsoft Entra ID, Google Workspace)? (D-06)
8. Are there legal, auditing or data-hosting requirements? (A-10)
9. Do you need access on mobile devices? (A-07)
10. Is there a deadline or budget for the MVP?
