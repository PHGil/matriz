-- Matriz schema v1 (docs/database.md). Enums are varchar + CHECK to keep the JPA mapping simple.

CREATE TABLE app_user (
    id         uuid         PRIMARY KEY,
    name       varchar(255) NOT NULL,
    email      varchar(254) NOT NULL,
    active     boolean      NOT NULL DEFAULT true,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_app_user_email ON app_user (lower(email));

CREATE TABLE project (
    id          uuid          PRIMARY KEY,
    name        varchar(100)  NOT NULL CHECK (char_length(btrim(name)) BETWEEN 3 AND 100),
    description varchar(1000),
    created_at  timestamptz   NOT NULL DEFAULT now(),
    updated_at  timestamptz   NOT NULL DEFAULT now()
);

CREATE TABLE project_member (
    project_id uuid        NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    user_id    uuid        NOT NULL REFERENCES app_user (id),
    role       varchar(10) NOT NULL CHECK (role IN ('MANAGER', 'MEMBER')),
    added_at   timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (project_id, user_id)
);
CREATE UNIQUE INDEX ux_project_single_manager ON project_member (project_id) WHERE role = 'MANAGER';
CREATE INDEX ix_project_member_user ON project_member (user_id);

CREATE TABLE task (
    id          uuid          PRIMARY KEY,
    project_id  uuid          NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    title       varchar(150)  NOT NULL CHECK (char_length(btrim(title)) BETWEEN 3 AND 150),
    description varchar(2000),
    status      varchar(20)   NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    priority    varchar(10)   NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    due_date    date,
    created_at  timestamptz   NOT NULL DEFAULT now(),
    updated_at  timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX ix_task_project_status ON task (project_id, status);

CREATE TABLE task_assignee (
    task_id     uuid        NOT NULL REFERENCES task (id) ON DELETE CASCADE,
    user_id     uuid        NOT NULL REFERENCES app_user (id),
    assigned_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (task_id, user_id)
);
CREATE INDEX ix_task_assignee_user ON task_assignee (user_id);

CREATE TABLE task_comment (
    id         uuid          PRIMARY KEY,
    task_id    uuid          NOT NULL REFERENCES task (id) ON DELETE CASCADE,
    author_id  uuid          NOT NULL REFERENCES app_user (id),
    content    varchar(2000) NOT NULL CHECK (char_length(btrim(content)) >= 1),
    created_at timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX ix_task_comment_task ON task_comment (task_id, created_at);
