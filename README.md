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

| File                                         | Contents                                                                                     |
| -------------------------------------------- | -------------------------------------------------------------------------------------------- |
| [docs/requirements.md](docs/requirements.md) | Vision, user profiles, functional and non-functional requirements, MVP scope.                |
| [docs/user-stories.md](docs/user-stories.md) | Backlog, user stories, acceptance criteria and exceptional cases.                            |
| [docs/constraints.md](docs/constraints.md)   | Constraints, assumptions, security and privacy, pending decisions, questions for the client. |
| [docs/architecture.md](docs/architecture.md) | Architecture decision (modular monolith), components, ADRs, consistency review.              |
| [docs/database.md](docs/database.md)         | Data model, business rules and Mermaid ER diagram.                                           |
| [openapi.yaml](openapi.yaml)                 | OpenAPI 3.1 contract (login, create/update task, project progress).                          |
| [docs/ui/](docs/ui/)                         | Low-fidelity wireframe of the project board (`wireframe.svg` / `wireframe.png`).             |
| [AGENTS.md](AGENTS.md)                       | Instructions for AI agents working in this repository.                                       |

## Current status

**Architecture phase (v0.1).** There is no code yet. The requirements are based on a short client description and a set of assumptions still to be validated (see [docs/constraints.md](docs/constraints.md#2-assumptions)). Architecture, data model, API contract and wireframe are defined (Architecture Pack); implementation is the next phase.

**Planned stack:** Java + Quarkus (REST API), Angular (SPA), PostgreSQL.
