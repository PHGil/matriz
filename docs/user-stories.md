# Matriz — User Stories (Initial backlog)

> Priorities in MoSCoW (M = Must, S = Should). Requirement IDs refer to [requirements.md](requirements.md) and assumptions to [constraints.md](constraints.md).

## Backlog summary

| ID    | User Story (summary)                | Profile         | Priority | Requirements        | MVP      |
| ----- | ----------------------------------- | --------------- | -------- | ------------------- | -------- |
| US-01 | Log in and log out                  | User            | M        | FR-01, FR-02        | Yes      |
| US-02 | Create and deactivate user accounts | Administrator   | M        | FR-03, FR-04        | Yes      |
| US-03 | Create a project                    | User            | M        | FR-10, FR-13        | Yes      |
| US-04 | Manage project members              | Project Manager | M        | FR-12               | Yes      |
| US-05 | Create a task                       | Team Member     | M        | FR-20, FR-22, FR-26 | Yes      |
| US-06 | Change a task's status on the board | Team Member     | M        | FR-23, FR-30        | Yes      |
| US-07 | View a project's progress           | Team Member     | M        | FR-31               | Yes      |
| US-08 | Delete a task                       | Project Manager | M        | FR-25               | Yes      |
| US-09 | Filter a project's tasks            | Team Member     | M        | FR-32               | Yes      |
| US-10 | View "My tasks"                     | Team Member     | S        | FR-33               | Post-MVP |
| US-11 | Archive a project                   | Project Manager | S        | FR-14               | Post-MVP |
| US-12 | Manage task assignees               | Team Member     | M        | FR-24               | Yes      |
| US-13 | Comment on a task                   | Team Member     | M        | FR-27               | Yes      |
| US-14 | Delete a project                    | Project Manager | M        | FR-15               | Yes      |
| US-15 | Search projects and see dashboard   | User            | M        | FR-16, FR-34        | Yes      |

> US-09 and US-12 – US-15 were added or promoted in Class 3 (see [implementation-plan.md](implementation-plan.md)).

---

## MVP User Stories

### US-01 — Log in and log out (M)

**As a** user, **I want** to log in with my username and password and log out whenever I want, **so that** I can access only my work securely.

**Acceptance criteria**

1. **Given** an active account with username `ana`, **When** the user submits the correct username and password, **Then** she sees the list of her projects.
2. **Given** an active account, **When** a wrong password is submitted, **Then** access is denied with the message "Invalid username or password" (the same message used for non-existent usernames).
3. *(Removed in Class 3 together with NFR-04.)*
4. **Given** a deactivated account, **When** correct credentials are submitted, **Then** access is denied.
5. **Given** an authenticated user, **When** they log out, **Then** they are redirected to the login screen and any protected page requires authenticating again.

### US-02 — Create and deactivate accounts (M)

**As an** Administrator, **I want** to create and deactivate user accounts, **so that** I control who has access to Matriz.

**Acceptance criteria**

1. **Given** an authenticated Administrator, **When** they create an account with a valid name and email, **Then** the account is active and the user can log in.
2. **Given** an account with email `ana@company.com` already exists, **When** the Administrator tries to create another with the same email (including upper/lower-case variations), **Then** creation is refused with the message "An account with this email already exists".
3. **Given** a user with an active session, **When** the Administrator deactivates their account, **Then** the user loses access within the access-token lifetime (5 minutes, ADR-08) and their tasks remain unchanged.
4. **Given** a user without the Administrator profile, **When** they try to access account management (via the UI or directly on the server), **Then** access is denied.

> Note (Class 3): account management is done in the Keycloak admin console of the `matriz` realm; the Administrator sets an initial password there (IMP-11). AC2's message is Keycloak's equivalent.

### US-03 — Create a project (M)

**As a** user, **I want** to create a project, **so that** I can organise my team's tasks in a single place.

**Acceptance criteria**

1. **Given** an authenticated user, **When** they create a project named "Website 2027", **Then** the project appears in their project list and the user becomes its Manager.
2. **Given** the creation form, **When** the name is empty, only whitespace, shorter than 3 or longer than 100 characters, **Then** the project is not created and the error is shown on the name field.
3. **Given** a project created by Ana, **When** Bruno (not a member) views his project list or tries to open the project via its direct URL, **Then** the project is not shown to him and direct access shows "Not found" (EC-05).

### US-04 — Manage project members (M)

**As a** Project Manager, **I want** to add and remove members, **so that** only the right people work on the project.

**Acceptance criteria**

1. **Given** the project Manager, **When** they add an active user, **Then** that user sees the project in their list.
2. **Given** a user who is already a member, **When** the Manager tries to add them again, **Then** no duplicate is created.
3. **Given** a member assigned to 3 tasks, **When** the Manager removes them from the project, **Then** the member loses access to the project and is removed as assignee from those 3 tasks.
4. **Given** the project Manager, **When** they try to remove themselves, **Then** the operation is refused (a project always has a Manager).
5. **Given** a Member (not Manager), **When** they try to add or remove members, **Then** the operation is refused.

### US-05 — Create a task (M)

**As a** team member, **I want** to create tasks in a project, **so that** I can record the work that needs to be done.

**Acceptance criteria**

1. **Given** a project member, **When** they create a task with only the title "Review proposal", **Then** the task is created with status "To Do", priority "Medium", no assignees, and appears in the "To Do" column of the board.
2. **Given** the task form, **When** the title is empty, only whitespace, shorter than 3 or longer than 150 characters, or the description exceeds 2000 characters, **Then** the task is not created and the error is shown.
3. *(Moved to US-12.)*
4. **Given** a project member, **When** they set a due date earlier than today, **Then** the task is created with that due date (allows recording work that is already late).

### US-06 — Change a task's status (M)

**As a** team member, **I want** to change a task's status on the board, **so that** everyone knows where the work stands.

**Acceptance criteria**

1. **Given** a task in "To Do", **When** a member moves it to "In Progress", **Then** the task appears in the "In Progress" column and its last-modified date is updated.
2. **Given** a task in "Done", **When** a member moves it to "To Do" or "In Progress", **Then** the change is allowed (reopening).
3. **Given** a user who is not a project member, **When** they try to change a task's status directly on the server, **Then** the operation is refused.

### US-07 — View a project's progress (M)

**As a** team member, **I want** to see a project's progress, **so that** I can quickly understand how much work is left.

**Acceptance criteria**

1. **Given** a project with 10 tasks (2 "To Do", 3 "In Progress", 5 "Done"), **When** a member opens the project, **Then** they see the counts 2 / 3 / 5 and progress "50%".
2. **Given** a project with no tasks, **When** a member opens the project, **Then** they see "No tasks" instead of a percentage.
3. **Given** a project with 3 tasks and 1 done, **When** progress is displayed, **Then** the value is rounded to the nearest whole number ("33%").
4. **Given** a task is deleted or changes status, **When** progress is viewed again, **Then** it reflects the change.

### US-08 — Delete a task (M)

**As a** Project Manager, **I want** to delete tasks created by mistake, **so that** the project stays clean.

**Acceptance criteria**

1. **Given** the project Manager, **When** they delete a task and confirm the action, **Then** the task no longer appears on the board or in the progress counts.
2. **Given** the project Manager, **When** they cancel the confirmation, **Then** the task is not deleted.
3. **Given** a Member (not Manager), **When** they try to delete a task, **Then** the operation is refused.

---

## User Stories added in Class 3 (M)

### US-09 — Filter tasks (M)

**As a** team member, **I want** to filter a project's tasks by status and priority, **so that** I can quickly find what matters to me.

1. **Given** a project with tasks of several priorities, **When** a member filters by priority "High", **Then** only high-priority tasks are shown.
2. **Given** an active filter, **When** progress counts are shown, **Then** they still reflect all tasks of the project.

### US-12 — Manage task assignees (M)

**As a** team member, **I want** to assign one or more project members to a task and remove assignments, **so that** everyone knows who is responsible.

1. **Given** a task, **When** a member assigns two project members, **Then** both appear as assignees after reopening the task.
2. **Given** a user already assigned to a task, **When** they are assigned again, **Then** the operation is refused and no duplicate is created.
3. **Given** a user who is not an active project member, **When** someone tries to assign them, **Then** the operation is refused.
4. **Given** an assignment, **When** a member removes it, **Then** the user is no longer an assignee.

### US-13 — Comment on a task (M)

**As a** team member, **I want** to add comments to a task, **so that** the team can discuss the work in context.

1. **Given** a task, **When** a member adds a comment, **Then** it is listed with the author's name and date, taken from the authenticated session.
2. **Given** the comment form, **When** the content is empty, only whitespace or longer than 2000 characters, **Then** the comment is not saved.

### US-14 — Delete a project (M)

**As a** Project Manager, **I want** to delete a project, **so that** projects created by mistake do not remain.

1. **Given** the Manager, **When** they confirm the deletion, **Then** the project, its members, tasks, assignments and comments are deleted.
2. **Given** the Manager, **When** they cancel the confirmation, **Then** nothing is deleted.
3. **Given** a Member (not Manager), **When** they try to delete the project, **Then** the operation is refused.

### US-15 — Search projects and see dashboard (M)

**As a** user, **I want** to see my projects with task counts per status and search them by name, **so that** I can get to the right project quickly.

1. **Given** an authenticated user, **When** they open the home page, **Then** they see only their projects with the number of tasks "To Do", "In Progress" and "Done", and can open each one.
2. **Given** projects "Website 2027" and "Intranet", **When** the user searches "web", **Then** only "Website 2027" is shown (case-insensitive).

---

## Post-MVP User Stories (S)

### US-10 — My tasks (S)
**As a** team member, **I want** to see all tasks assigned to me across all projects, **so that** I know what I have pending.

### US-11 — Archive project (S)
**As a** Project Manager, **I want** to archive a finished project, **so that** it no longer appears in the active project list without losing its information.

*(Acceptance criteria to be detailed when prioritised.)*

---

## Exceptional and edge cases

| ID    | Situation                                                                | Expected behaviour                                                                       | US           |
| ----- | ------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------- | ------------ |
| EC-01 | Two members edit the same task almost simultaneously.                    | Last write wins; no data is corrupted. *(Conflict strategy to be confirmed in Class 2.)* | US-06        |
| EC-02 | A task assignee is removed from the project.                             | Their assignments in that project are removed.                                           | US-04        |
| EC-03 | The assignee's account is deactivated.                                   | Tasks keep the assignment, shown as "(deactivated)"; the user cannot be newly assigned.  | US-02        |
| EC-04 | The Manager tries to remove themselves from the project.                 | Refused; the project always has a Manager.                                               | US-04        |
| EC-05 | Access to a non-existent or unauthorised project or task via direct URL. | Generic "Not found" message, without revealing whether the resource exists.              | US-03, US-06 |
| EC-06 | Project with no tasks.                                                   | Progress shown as "No tasks" (avoids division by zero).                                  | US-07        |
| EC-07 | Session expires while the user is editing a task.                        | Saving is refused and the user is sent to the login screen.                              | US-01        |
| EC-08 | Title containing HTML/script content (e.g. `<script>`).                  | Stored and displayed as text, never executed.                                            | US-05        |
| EC-09 | Due date in the past.                                                    | Accepted without error. *(Visual highlighting of overdue tasks is not in the MVP.)*      | US-05        |
