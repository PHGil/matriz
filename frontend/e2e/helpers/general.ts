import { execFileSync } from 'node:child_process';
import { resolve } from 'node:path';

const TRUNCATE = 'TRUNCATE task_comment, task_assignee, task, project_member, project, app_user';

// Empties the dedicated test database (matriz_st) through the Postgres container; credentials stay in the container.
export const resetDB = () =>
  execFileSync(
    'docker',
    [
      'compose',
      'exec',
      '-T',
      'postgres',
      'sh',
      '-c',
      `psql -U "$POSTGRES_USER" -d matriz_st -c "${TRUNCATE}"`,
    ],
    { cwd: resolve(__dirname, '..', '..', '..'), stdio: 'ignore' },
  );
