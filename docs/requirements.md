# Matriz — Requirements

> Status: **Draft v0.1** — based on a minimal client description. Anything not directly derived from that description is an **assumption** (see [constraints.md](constraints.md)) and must be validated with the client.

## 1. Vision and goals

**Original client description (the only confirmed fact):**

> "We need an application where our teams can organise projects, manage tasks and track the progress of the work."

**Vision:** Matriz is a project and task management application that lets an organisation's teams centralise their work, know who is doing what, and track the progress of each project.

**Goals:**

| ID   | Goal                                                       | Source      |
|------|------------------------------------------------------------|-------------|
| G-1  | Allow work to be organised into projects                   | Client      |
| G-2  | Allow tasks to be created, assigned and updated within projects | Client |
| G-3  | Allow the progress of each project to be tracked           | Client      |
| G-4  | Restrict access to each project to its members             | Assumption (A-04) |

## 2. User types

| Profile          | Description                                                                 | Source            |
|------------------|-----------------------------------------------------------------------------|-------------------|
| Administrator    | Manages the organisation's user accounts. Has no automatic access to project content. | Assumption (A-03) |
| Project Manager  | Creates projects, manages their members and has full control over the tasks in those projects. | Assumption (A-05) |
| Team Member      | Takes part in projects they were added to: creates, edits and updates tasks. | Assumption (A-05) |

The same user can be Manager in one project and Member in another. The Manager role is assigned **per project** (whoever creates the project becomes its Manager).

## 3. Functional requirements

Priority using MoSCoW: **M** (Must), **S** (Should), **C** (Could), **W** (Won't — for now).

### 3.1 Accounts and authentication

| ID    | Requirement | Priority | Rationale |
|-------|-------------|----------|-----------|
| FR-01 | The system must allow authentication with email and password. | M | Needed to identify users and control access. |
| FR-02 | The system must allow users to log out. | M | Basic security on shared workstations. |
| FR-03 | The Administrator must be able to create user accounts (name, email). | M | Internal tool: access is controlled by the organisation (A-02). |
| FR-04 | The Administrator must be able to deactivate and reactivate accounts. | M | Allows revoking access (e.g. employees leaving) without losing history. |
| FR-05 | Users must be able to change their own password. | S | Security hygiene; does not block initial use. |

### 3.2 Projects

| ID    | Requirement | Priority | Rationale |
|-------|-------------|----------|-----------|
| FR-10 | Any active user must be able to create a project (name required, description optional), becoming its Manager. | M | G-1. |
| FR-11 | The Manager must be able to edit the project's name and description. | M | G-1. |
| FR-12 | The Manager must be able to add and remove project members. | M | G-4; team collaboration. |
| FR-13 | Users must only see the projects they are a member or Manager of. | M | G-4. |
| FR-14 | The Manager must be able to archive a project, making it read-only. | S | Keeps the project list clean without losing information. |

### 3.3 Tasks

| ID    | Requirement | Priority | Rationale |
|-------|-------------|----------|-----------|
| FR-20 | Members and the Manager must be able to create tasks in a project (title required; description, assignee, priority and due date optional). | M | G-2. |
| FR-21 | Members and the Manager must be able to edit a task's fields. | M | G-2. |
| FR-22 | Each task has exactly one status out of: **To Do**, **In Progress**, **Done**. New tasks start as "To Do". | M | Basis for tracking progress (G-3). |
| FR-23 | Members and the Manager must be able to change a task's status. | M | G-2, G-3. |
| FR-24 | A task can have at most one assignee, who must be a project member. | M | Clarity on "who does what". |
| FR-25 | Only the Manager can delete tasks. | M | Prevents accidental loss of information. |
| FR-26 | A task's priority, when set, is one of: **Low**, **Medium**, **High**. | M | Helps order work. |

### 3.4 Views and progress

| ID    | Requirement | Priority | Rationale |
|-------|-------------|----------|-----------|
| FR-30 | The system must display a project's tasks on a board with one column per status. | M | Common, quick way to see the state of work (G-3). |
| FR-31 | The system must display, per project, the number of tasks in each status and the percentage of tasks done. | M | G-3. |
| FR-32 | Users must be able to filter a project's tasks by assignee, status and priority. | S | Eases management of projects with many tasks. |
| FR-33 | Users must have a "My tasks" view listing the tasks assigned to them across all their projects. | S | Personal view of pending work. |

## 4. Non-functional requirements

| ID     | Category        | Requirement | Verification |
|--------|-----------------|-------------|--------------|
| NFR-01 | Security        | Passwords are stored only as a strong salted hash (never in plain text). | Database inspection / code review. |
| NFR-02 | Security        | All client-server communication is encrypted (HTTPS/TLS). | Plain HTTP requests are rejected or redirected. |
| NFR-03 | Security        | Access control is enforced on the server for every operation (not only in the UI). | Tests sending direct requests without permission receive an authorisation error. |
| NFR-04 | Security        | After 5 consecutive failed login attempts, the account is locked for login for 15 minutes. | Automated test. |
| NFR-05 | Usability       | The user interface is in Portuguese (Portugal). | UI review. |
| NFR-06 | Compatibility   | The application works on the two latest stable versions of Chrome, Firefox, Edge and Safari, on desktop screens (≥ 1280px). | Manual/automated tests on the listed browsers. |
| NFR-07 | Performance     | The board of a project with up to 500 tasks loads in under 2 seconds under normal network conditions. | Performance test. *(Target value is an assumption — A-08.)* |
| NFR-08 | Reliability     | Every task and project records its creation and last-modified dates. | Data check. |
| NFR-09 | Maintainability | The code has automated tests for business and permission rules. | Test pipeline. |

> Note: UI labels and messages are written in English in this documentation for readability; the UI will use the Portuguese equivalents (NFR-05).

## 5. MVP scope

The MVP includes **all requirements with priority M**:

- Authentication and account management by the Administrator (FR-01 to FR-04).
- Creating and managing projects and their members (FR-10 to FR-13).
- Task management with fixed statuses, assignee and priority (FR-20 to FR-26).
- Board by status and progress indicator per project (FR-30, FR-31).
- Non-functional requirements NFR-01 to NFR-09.

Requirements with priority **S** (FR-05, FR-14, FR-32, FR-33) are the first candidates right after the MVP, or within the MVP if there is capacity.

## 6. Out of initial scope (W)

| Feature | Reason for deferring |
|---------|----------------------|
| User self-registration | Internal tool; accounts created by the Administrator (A-02). |
| Password recovery by email | Depends on an email service; the Administrator can reset it. **Pending decision** (D-05). |
| Sprints, epics, roadmaps | Not requested by the client; add complexity. |
| Customisable statuses/workflows | Fixed statuses are enough to measure progress. |
| Subtasks, dependencies, labels | Not essential for the initial goal. |
| Task comments and attachments | Useful but not essential; attachments bring storage and security risks. |
| Notifications (email, push) | Require additional infrastructure. |
| Time tracking | Not requested. |
| Advanced reports and dashboards | The MVP covers basic progress (FR-31). |
| Integrations (Git, Slack, calendar) and public API | Not requested. |
| SSO / two-factor authentication | To be assessed according to the organisation's policy (D-06). |
| Native mobile app / mobile layout | Target audience assumed to be on desktop (A-07). |
| Multiple organisations (multi-tenant) | A single organisation is assumed (A-01). |
