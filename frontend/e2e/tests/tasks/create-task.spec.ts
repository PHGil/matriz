import { test } from '../../fixtures';
import { createProject } from '../../helpers/api';
import { resetDB } from '../../helpers/general';
import { NEW_TASK, PROJECT } from '../../test-data';

test.beforeEach(async () => {
  resetDB();
  await createProject('ana', PROJECT.name);
});

// US-01, US-03, US-05: log in -> open the project -> create a task -> see it in the "To Do" column.
test('should log in, open a project, create a task and see it in the To Do column', async ({
  loginPage,
  projectsPage,
  projectPage,
}) => {
  await test.step('log in as Ana through Keycloak', async () => {
    await loginPage.goto();
    await loginPage.login('ana');
    await loginPage.checkLoggedIn('ana');
  });

  await test.step('open the project from the project list', async () => {
    await projectsPage.goto();
    await projectsPage.openProject(PROJECT.name);
    await projectPage.checkPage(PROJECT.name);
  });

  await test.step('create the task', async () => {
    await projectPage.createTask(NEW_TASK.title);
    await projectPage.checkTaskCreated(NEW_TASK.title);
  });

  await test.step('see the task in the To Do column', async () => {
    await projectPage.checkTaskInColumn(NEW_TASK.title, NEW_TASK.column);
    await projectPage.checkColumnCount(NEW_TASK.column, 1);
  });
});

test('should not create a task with a too short title', async ({
  loginPage,
  projectsPage,
  projectPage,
}) => {
  await loginPage.goto();
  await loginPage.login('ana');
  await projectsPage.goto();
  await projectsPage.openProject(PROJECT.name);

  await projectPage.createTask('ab');

  await projectPage.checkTitleError();
  await projectPage.checkColumnCount(NEW_TASK.column, 0);
});
