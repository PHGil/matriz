-- Demo data (dev profile only). Repeatable and idempotent: re-runs only when this file changes and never deletes data.
-- User ids match keycloak/matriz-realm.json.

INSERT INTO app_user (id, name, email) VALUES
    ('9a6f0c2e-0000-4000-8000-000000000001', 'Admin Matriz', 'admin@matriz.test'),
    ('9a6f0c2e-0000-4000-8000-000000000011', 'Ana Silva',    'ana@matriz.test'),
    ('9a6f0c2e-0000-4000-8000-000000000012', 'Bruno Costa',  'bruno@matriz.test'),
    ('9a6f0c2e-0000-4000-8000-000000000013', 'Carla Dias',   'carla@matriz.test')
ON CONFLICT DO NOTHING;

-- Diogo is not in Keycloak (cannot log in) and is inactive: shows the "desativado" badge on tasks (EC-03).
INSERT INTO app_user (id, name, email, active) VALUES
    ('9a6f0c2e-0000-4000-8000-000000000014', 'Diogo Antunes', 'diogo@matriz.test', false)
ON CONFLICT DO NOTHING;

-- Website 2027: Ana (Manager), Bruno (Member). Carla has no access.
-- Intranet: Bruno (Manager), Carla (Member), Diogo (inactive Member). Ana has no access.
-- Campanha de Natal: Carla (Manager), Ana (Member); no tasks (empty state).
-- Migração de servidores: Ana (Manager), Bruno (Member); all tasks done (100%).
INSERT INTO project (id, name, description) VALUES
    ('7c1d4b3a-0000-4000-8000-000000000101', 'Website 2027', 'Redesenho do site institucional'),
    ('7c1d4b3a-0000-4000-8000-000000000102', 'Intranet',     'Portal interno da equipa'),
    ('7c1d4b3a-0000-4000-8000-000000000103', 'Campanha de Natal', 'Comunicação interna de fim de ano'),
    ('7c1d4b3a-0000-4000-8000-000000000104', 'Migração de servidores', 'Concluído em outubro')
ON CONFLICT DO NOTHING;

INSERT INTO project_member (project_id, user_id, role) VALUES
    ('7c1d4b3a-0000-4000-8000-000000000101', '9a6f0c2e-0000-4000-8000-000000000011', 'MANAGER'),
    ('7c1d4b3a-0000-4000-8000-000000000101', '9a6f0c2e-0000-4000-8000-000000000012', 'MEMBER'),
    ('7c1d4b3a-0000-4000-8000-000000000102', '9a6f0c2e-0000-4000-8000-000000000012', 'MANAGER'),
    ('7c1d4b3a-0000-4000-8000-000000000102', '9a6f0c2e-0000-4000-8000-000000000013', 'MEMBER'),
    ('7c1d4b3a-0000-4000-8000-000000000102', '9a6f0c2e-0000-4000-8000-000000000014', 'MEMBER'),
    ('7c1d4b3a-0000-4000-8000-000000000103', '9a6f0c2e-0000-4000-8000-000000000013', 'MANAGER'),
    ('7c1d4b3a-0000-4000-8000-000000000103', '9a6f0c2e-0000-4000-8000-000000000011', 'MEMBER'),
    ('7c1d4b3a-0000-4000-8000-000000000104', '9a6f0c2e-0000-4000-8000-000000000011', 'MANAGER'),
    ('7c1d4b3a-0000-4000-8000-000000000104', '9a6f0c2e-0000-4000-8000-000000000012', 'MEMBER')
ON CONFLICT DO NOTHING;

-- Local testing only: the admin account is a Member of every project so it can see all the data.
INSERT INTO project_member (project_id, user_id, role)
SELECT p.id, '9a6f0c2e-0000-4000-8000-000000000001', 'MEMBER'
  FROM project p
 WHERE p.id IN ('7c1d4b3a-0000-4000-8000-000000000101', '7c1d4b3a-0000-4000-8000-000000000102',
                '7c1d4b3a-0000-4000-8000-000000000103', '7c1d4b3a-0000-4000-8000-000000000104')
ON CONFLICT DO NOTHING;

INSERT INTO task (id, project_id, title, description, status, priority, due_date) VALUES
    ('5e2a9f10-0000-4000-8000-000000001001', '7c1d4b3a-0000-4000-8000-000000000101', 'Rever proposta',              NULL,                         'TODO',        'MEDIUM', NULL),
    ('5e2a9f10-0000-4000-8000-000000001002', '7c1d4b3a-0000-4000-8000-000000000101', 'Definir mapa do site',        'Estrutura das páginas',      'TODO',        'HIGH',   DATE '2026-11-15'),
    ('5e2a9f10-0000-4000-8000-000000001003', '7c1d4b3a-0000-4000-8000-000000000101', 'Maquetes da página inicial',  NULL,                         'IN_PROGRESS', 'MEDIUM', DATE '2026-11-05'),
    ('5e2a9f10-0000-4000-8000-000000001004', '7c1d4b3a-0000-4000-8000-000000000101', 'Levantamento de requisitos',  NULL,                         'DONE',        'HIGH',   DATE '2026-10-01'),
    ('5e2a9f10-0000-4000-8000-000000001005', '7c1d4b3a-0000-4000-8000-000000000102', 'Configurar autenticação',     NULL,                         'IN_PROGRESS', 'HIGH',   NULL),
    ('5e2a9f10-0000-4000-8000-000000001006', '7c1d4b3a-0000-4000-8000-000000000102', 'Publicar notícias',           NULL,                         'TODO',        'LOW',    NULL)
ON CONFLICT DO NOTHING;

-- Due dates are relative to the first run, so there is always one overdue and some upcoming tasks.
INSERT INTO task (id, project_id, title, description, status, priority, due_date) VALUES
    ('5e2a9f10-0000-4000-8000-000000001007', '7c1d4b3a-0000-4000-8000-000000000101', 'Escolher paleta de cores',            'Alinhar com a identidade visual da marca.', 'IN_PROGRESS', 'LOW',    CURRENT_DATE + 7),
    ('5e2a9f10-0000-4000-8000-000000001008', '7c1d4b3a-0000-4000-8000-000000000101', 'Corrigir contraste dos botões',      'Falha de acessibilidade detetada na revisão.', 'TODO',        'HIGH',   CURRENT_DATE - 3),
    ('5e2a9f10-0000-4000-8000-000000001009', '7c1d4b3a-0000-4000-8000-000000000101', 'Escrever textos da página "Sobre nós"', E'Incluir:\n- história da empresa\n- equipa\n- valores', 'TODO', 'MEDIUM', NULL),
    ('5e2a9f10-0000-4000-8000-000000001010', '7c1d4b3a-0000-4000-8000-000000000101', 'Configurar domínio e certificado',   NULL,                                         'DONE',        'MEDIUM', CURRENT_DATE - 20),
    ('5e2a9f10-0000-4000-8000-000000001011', '7c1d4b3a-0000-4000-8000-000000000102', 'Migrar utilizadores antigos',        'Exportar contas do sistema anterior.',       'TODO',        'HIGH',   CURRENT_DATE + 14),
    ('5e2a9f10-0000-4000-8000-000000001012', '7c1d4b3a-0000-4000-8000-000000000102', 'Formação da equipa',                NULL,                                         'DONE',        'LOW',    CURRENT_DATE - 10),
    ('5e2a9f10-0000-4000-8000-000000001013', '7c1d4b3a-0000-4000-8000-000000000102', 'Definir permissões por departamento', NULL,                                       'IN_PROGRESS', 'MEDIUM', NULL),
    ('5e2a9f10-0000-4000-8000-000000001014', '7c1d4b3a-0000-4000-8000-000000000104', 'Inventariar serviços',              NULL,                                         'DONE',        'HIGH',   CURRENT_DATE - 40),
    ('5e2a9f10-0000-4000-8000-000000001015', '7c1d4b3a-0000-4000-8000-000000000104', 'Desligar servidor antigo',           NULL,                                         'DONE',        'MEDIUM', CURRENT_DATE - 30)
ON CONFLICT DO NOTHING;

INSERT INTO task_assignee (task_id, user_id) VALUES
    ('5e2a9f10-0000-4000-8000-000000001002', '9a6f0c2e-0000-4000-8000-000000000012'),
    ('5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000011'),
    ('5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000012'),
    ('5e2a9f10-0000-4000-8000-000000001005', '9a6f0c2e-0000-4000-8000-000000000013'),
    ('5e2a9f10-0000-4000-8000-000000001007', '9a6f0c2e-0000-4000-8000-000000000011'),
    ('5e2a9f10-0000-4000-8000-000000001008', '9a6f0c2e-0000-4000-8000-000000000012'),
    ('5e2a9f10-0000-4000-8000-000000001010', '9a6f0c2e-0000-4000-8000-000000000011'),
    ('5e2a9f10-0000-4000-8000-000000001010', '9a6f0c2e-0000-4000-8000-000000000012'),
    ('5e2a9f10-0000-4000-8000-000000001011', '9a6f0c2e-0000-4000-8000-000000000012'),
    ('5e2a9f10-0000-4000-8000-000000001011', '9a6f0c2e-0000-4000-8000-000000000013'),
    ('5e2a9f10-0000-4000-8000-000000001012', '9a6f0c2e-0000-4000-8000-000000000013'),
    ('5e2a9f10-0000-4000-8000-000000001012', '9a6f0c2e-0000-4000-8000-000000000014'),
    ('5e2a9f10-0000-4000-8000-000000001013', '9a6f0c2e-0000-4000-8000-000000000013'),
    ('5e2a9f10-0000-4000-8000-000000001014', '9a6f0c2e-0000-4000-8000-000000000011'),
    ('5e2a9f10-0000-4000-8000-000000001015', '9a6f0c2e-0000-4000-8000-000000000012')
ON CONFLICT DO NOTHING;

INSERT INTO task_comment (id, task_id, author_id, content) VALUES
    ('3b8c6d20-0000-4000-8000-000000002001', '5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000011', 'Primeira versão pronta para revisão.'),
    ('3b8c6d20-0000-4000-8000-000000002002', '5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000012', 'Obrigado, vou rever amanhã.'),
    ('3b8c6d20-0000-4000-8000-000000002003', '5e2a9f10-0000-4000-8000-000000001003', '9a6f0c2e-0000-4000-8000-000000000011', 'Combinado. Conseguimos fechar esta semana?'),
    ('3b8c6d20-0000-4000-8000-000000002004', '5e2a9f10-0000-4000-8000-000000001008', '9a6f0c2e-0000-4000-8000-000000000011', 'Prioritário: falha de acessibilidade a corrigir antes do lançamento.'),
    ('3b8c6d20-0000-4000-8000-000000002005', '5e2a9f10-0000-4000-8000-000000001011', '9a6f0c2e-0000-4000-8000-000000000012', 'Quem tem acesso ao sistema anterior?'),
    ('3b8c6d20-0000-4000-8000-000000002006', '5e2a9f10-0000-4000-8000-000000001011', '9a6f0c2e-0000-4000-8000-000000000013', 'Eu. Já tenho a lista exportada.')
ON CONFLICT DO NOTHING;
