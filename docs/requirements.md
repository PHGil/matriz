# Matriz — Requirements

> Status: **Draft v0.1** — based on a minimal client description. Anything not directly derived from that description is an **assumption** (see [constraints.md](constraints.md)) and must be validated with the client.

## 1. Vision and goals

**Original client description (the only confirmed fact):**

> "We need an application where our teams can organise projects, manage tasks and track the progress of the work."

**Vision:** Matriz is a project and task management application that lets an organisation's teams centralise their work, know who is doing what, and track the progress of each project.

**Goals:**

| ID  | Goal                                                            | Source            |
| --- | --------------------------------------------------------------- | ----------------- |
| G-1 | Allow work to be organised into projects                        | Client            |
| G-2 | Allow tasks to be created, assigned and updated within projects | Client            |
| G-3 | Allow the progress of each project to be tracked                | Client            |
| G-4 | Restrict access to each project to its members                  | Assumption (A-04) |

## 2. User types

| Profile         | Description                                                                                    | Source            |
| --------------- | ---------------------------------------------------------------------------------------------- | ----------------- |
| Administrator   | Manages the organisation's user accounts. Has no automatic access to project content.          | Assumption (A-03) |
| Project Manager | Creates projects, manages their members and has full control over the tasks in those projects. | Assumption (A-05) |
| Team Member     | Takes part in projects they were added to: creates, edits and updates tasks.                   | Assumption (A-05) |

The same user can be Manager in one project and Member in another. The Manager role is assigned **per project** (whoever creates the project becomes its Manager).

## 3. Functional requirements

Priority using MoSCoW: **M** (Must), **S** (Should), **C** (Could), **W** (Won't — for now).

### 3.1 Accounts and authentication

| ID    | Requirement                                                           | Priority | Rationale                                                                                                            |
| ----- | --------------------------------------------------------------------- | -------- | -------------------------------------------------------------------------------------------------------------------- |
| FR-01 | The system must allow authentication with email and password.         | M        | Needed to identify users and control access.                                                                         |
| FR-02 | The system must allow users to log out.                               | M        | Basic security on shared workstations.                                                                               |
| FR-03 | The Administrator must be able to create user accounts (name, email). | M        | Internal tool: access is controlled by the organisation (A-02). Done in the Keycloak admin console (ADR-08).         |
| FR-04 | The Administrator must be able to deactivate and reactivate accounts. | M        | Allows revoking access (e.g. employees leaving) without losing history. Done in the Keycloak admin console (ADR-08). |
| FR-05 | Users must be able to change their own password.                      | S        | Security hygiene; does not block initial use.                                                                        |

### 3.2 Projects

| ID    | Requirement                                                                                                                                 | Priority | Rationale                                                |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------- | -------- | -------------------------------------------------------- |
| FR-10 | Any active user must be able to create a project (name required, 3–100 characters; description optional, up to 1000), becoming its Manager. | M        | G-1.                                                     |
| FR-11 | The Manager must be able to edit the project's name and description.                                                                        | M        | G-1.                                                     |
| FR-12 | The Manager must be able to add and remove project members.                                                                                 | M        | G-4; team collaboration.                                 |
| FR-13 | Users must only see the projects they are a member or Manager of.                                                                           | M        | G-4.                                                     |
| FR-14 | The Manager must be able to archive a project, making it read-only.                                                                         | S        | Keeps the project list clean without losing information. |
| FR-15 | The Manager must be able to delete a project, after confirmation; its members, tasks, assignments and comments are deleted with it.         | M        | Added in Class 3 (Lab Sheet 03).                         |
| FR-16 | Users must be able to search their projects by name.                                                                                        | M        | Added in Class 3 (Lab Sheet 03).                         |

### 3.3 Tasks

| ID    | Requirement                                                                                                                                                                     | Priority | Rationale                                                           |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- | ------------------------------------------------------------------- |
| FR-20 | Members and the Manager must be able to create tasks in a project (title required, 3–150 characters; description up to 2000, priority and due date optional).                   | M        | G-2.                                                                |
| FR-21 | Members and the Manager must be able to edit a task's fields.                                                                                                                   | M        | G-2.                                                                |
| FR-22 | Each task has exactly one status out of: **To Do**, **In Progress**, **Done**. New tasks start as "To Do".                                                                      | M        | Basis for tracking progress (G-3).                                  |
| FR-23 | Members and the Manager must be able to change a task's status.                                                                                                                 | M        | G-2, G-3.                                                           |
| FR-24 | A task can have several assignees; each must be an active project member and cannot be assigned twice to the same task. Members and the Manager can add and remove assignments. | M        | Clarity on "who does what". Changed in Class 3 (was "at most one"). |
| FR-25 | Only the Manager can delete tasks.                                                                                                                                              | M        | Prevents accidental loss of information.                            |
| FR-26 | A task's priority is one of: **Low**, **Medium**, **High**; default **Medium**.                                                                                                 | M        | Helps order work.                                                   |
| FR-27 | Members and the Manager must be able to add comments (1–2000 characters, not blank) to a task and list them with author and date.                                               | M        | Added in Class 3 (Lab Sheet 03).                                    |

### 3.4 Views and progress

| ID    | Requirement                                                                                                             | Priority | Rationale                                                                                             |
| ----- | ----------------------------------------------------------------------------------------------------------------------- | -------- | ----------------------------------------------------------------------------------------------------- |
| FR-30 | The system must display a project's tasks on a board with one column per status.                                        | M        | Common, quick way to see the state of work (G-3).                                                     |
| FR-31 | The system must display, per project, the number of tasks in each status and the percentage of tasks done.              | M        | G-3.                                                                                                  |
| FR-32 | Users must be able to filter a project's tasks by status and priority.                                                  | M        | Eases management of projects with many tasks. Promoted in Class 3; filter by assignee stays post-MVP. |
| FR-33 | Users must have a "My tasks" view listing the tasks assigned to them across all their projects.                         | S        | Personal view of pending work.                                                                        |
| FR-34 | After login, users see a dashboard with their projects and the number of tasks in each status, linking to each project. | M        | G-3. Added in Class 3 (Lab Sheet 03).                                                                 |

## 4. Non-functional requirements

| ID     | Category        | Requirement                                                                                                                                     | Verification                                                                     |
| ------ | --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| NFR-01 | Security        | Passwords are stored only as a strong salted hash (never in plain text).                                                                        | Provided by Keycloak (ADR-08); the application never stores passwords.           |
| NFR-02 | Security        | All client-server communication is encrypted (HTTPS/TLS).                                                                                       | Plain HTTP requests are rejected or redirected.                                  |
| NFR-03 | Security        | Access control is enforced on the server for every operation (not only in the UI).                                                              | Tests sending direct requests without permission receive an authorisation error. |
| NFR-04 | Security        | ~~After 5 consecutive failed login attempts, the account is locked for login for 15 minutes.~~ **Removed in Class 3** (product owner decision). | —                                                                                |
| NFR-05 | Usability       | The user interface is in Portuguese (Portugal).                                                                                                 | UI review.                                                                       |
| NFR-06 | Compatibility   | The application works on the two latest stable versions of Chrome, Firefox, Edge and Safari, on desktop screens (≥ 1280px).                     | Manual/automated tests on the listed browsers.                                   |
| NFR-07 | Performance     | The board of a project with up to 500 tasks loads in under 2 seconds under normal network conditions.                                           | Performance test. *(Target value is an assumption — A-08.)*                      |
| NFR-08 | Reliability     | Every task and project records its creation and last-modified dates.                                                                            | Data check.                                                                      |
| NFR-09 | Maintainability | The code has automated tests for business and permission rules.                                                                                 | Test pipeline.                                                                   |

> Note: UI labels and messages are written in English in this documentation for readability; the UI will use the Portuguese equivalents (NFR-05). Exception approved in Class 3: the Keycloak login pages are in English.

## 5. MVP scope

The MVP includes **all requirements with priority M**:

- Authentication (Keycloak) and account management by the Administrator in the Keycloak admin console (FR-01 to FR-04).
- Creating, searching, editing and deleting projects and managing their members (FR-10 to FR-13, FR-15, FR-16).
- Task management with fixed statuses, priority, several assignees and comments (FR-20 to FR-27).
- Board by status, filters, progress indicator and dashboard (FR-30 to FR-32, FR-34).
- Non-functional requirements NFR-01 to NFR-09, except NFR-04 (removed).

Requirements with priority **S** (FR-05, FR-14, FR-33) are the first candidates right after the MVP, or within the MVP if there is capacity.

> Class 3 change: FR-15, FR-16, FR-27, FR-34 added, FR-24 changed to several assignees, FR-32 promoted to M and field limits aligned with Lab Sheet 03. See [implementation-plan.md](implementation-plan.md#1-scope-reconciliation-lab-sheet-03-vs-approved-mvp).

## 6. Out of initial scope (W)

| Feature                                            | Reason for deferring                                                                      |
| -------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| User self-registration                             | Internal tool; accounts created by the Administrator (A-02).                              |
| Password recovery by email                         | Depends on an email service; the Administrator can reset it. **Pending decision** (D-05). |
| Sprints, epics, roadmaps                           | Not requested by the client; add complexity.                                              |
| Customisable statuses/workflows                    | Fixed statuses are enough to measure progress.                                            |
| Subtasks, dependencies, labels                     | Not essential for the initial goal.                                                       |
| Task attachments                                   | Attachments bring storage and security risks. (Comments moved into the MVP: FR-27.)       |
| Notifications (email, push)                        | Require additional infrastructure.                                                        |
| Time tracking                                      | Not requested.                                                                            |
| Advanced reports and dashboards                    | The MVP covers basic progress (FR-31).                                                    |
| Integrations (Git, Slack, calendar) and public API | Not requested.                                                                            |
| SSO / two-factor authentication                    | To be assessed according to the organisation's policy (D-06).                             |
| Native mobile app / mobile layout                  | Target audience assumed to be on desktop (A-07).                                          |
| Multiple organisations (multi-tenant)              | A single organisation is assumed (A-01).                                                  |
