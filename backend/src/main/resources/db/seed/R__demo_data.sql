-- Demo data (dev profile only). Repeatable and idempotent: re-runs only when this file changes and never deletes data.
-- User ids match keycloak/matriz-realm.json.

INSERT INTO app_user (id, name, email) VALUES
    ('9a6f0c2e-0000-4000-8000-000000000001', 'Admin Matriz', 'admin@matriz.test'),
    ('9a6f0c2e-0000-4000-8000-000000000011', 'Ana Silva',    'ana@matriz.test'),
    ('9a6f0c2e-0000-4000-8000-000000000012', 'Bruno Costa',  'bruno@matriz.test'),
    ('9a6f0c2e-0000-4000-8000-000000000013', 'Carla Dias',   'carla@matriz.test')
ON CONFLICT DO NOTHING;

-- Website 2027: Ana (Manager), Bruno (Member). Carla has no access.
-- Intranet: Bruno (Manager), Carla (Member). Ana has no access.
INSERT INTO project (id, name, description) VALUES
    ('7c1d4b3a-0000-4000-8000-000000000101', 'Website 2027', 'Redesenho do site institucional'),
    ('7c1d4b3a-0000-4000-8000-000000000102', 'Intranet',     'Portal interno da equipa')
ON CONFLICT DO NOTHING;

INSERT INTO project_member (project_id, user_id, role) VALUES
    ('7c1d4b3a-0000-4000-8000-000000000101', '9a6f0c2e-0000-4000-8000-000000000011', 'MANAGER'),
    ('7c1d4b3a-0000-4000-8000-000000000101', '9a6f0c2e-0000-4000-8000-000000000012', 'MEMBER'),
    ('7c1d4b3a-0000-4000-8000-000000000102', '9a6f0c2e-0000-4000-8000-000000000012', 'MANAGER'),
    ('7c1d4b3a-0000-4000-8000-000000000102', '9a6f0c2e-0000-4000-8000-000000000013', 'MEMBER')
ON CONFLICT DO NOTHING;

INSERT INTO task (id, project_id, title, description, status, priority, due_date) VALUES
    ('5e2a9f10-0000-4000-8000-000000001001', '7c1d4b3a-0000-4000-8000-000000000101', 'Rever proposta',              NULL,                         'TODO',        'MEDIUM', NULL),
    ('5e2a9f10-0000-4000-8000-000000001002', '7c1d4b3a-0000-4000-8000-000000000101', 'Definir mapa do site',        'Estrutura das páginas',      'TODO',        'HIGH',   DATE '2026-11-15'),
    ('5e2a9f10-0000-4000-8000-000000001003', '7c1d4b3a-0000-4000-8000-000000000101', 'Maquetes da página inicial',  NULL,                         'IN_PROGRESS', 'MEDIUM', DATE '2026-11-05'),
    ('5e2a9f10-0000-4000-8000-000000001004', '7c1d4b3a-0000-4000-8000-000000000101', 'Levantamento de requisitos',  NULL,                         'DONE',        'HIGH',   DATE '2026-10-01'),
    ('5e2a9f10-0000-4000-8000-000000001005', '7c1d4b3a-0000-4000-8000-000000000102', 'Configurar autenticação',     NULL,                         'IN_PROGRESS', 'HIGH',   NULL),
    ('5e2a9f10-0000-4000-8000-000000001006', '7c1d4b3a-0000-4000-8000-000000000102', 'Publicar notícias',           NULL,                         'TODO',        'LOW',    NULL)
ON CONFLICT DO NOTHING;

INSERT INTO task_assignee (task_id, user_id) VALUES
    ('5e2a9f10-0000-4000-8000-000000001002', '9a6f0c2e-0000-4000-8000-000000000012'),
    ('5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000011'),
    ('5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000012'),
    ('5e2a9f10-0000-4000-8000-000000001005', '9a6f0c2e-0000-4000-8000-000000000013')
ON CONFLICT DO NOTHING;

INSERT INTO task_comment (id, task_id, author_id, content) VALUES
    ('3b8c6d20-0000-4000-8000-000000002001', '5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000011', 'Primeira versão pronta para revisão.')
ON CONFLICT DO NOTHING;
