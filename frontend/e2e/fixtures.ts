export * from '@playwright/test';

import { test as base } from '@playwright/test';
import { LoginPage } from './pages/login-page';
import { ProjectPage } from './pages/project-page';
import { ProjectsPage } from './pages/projects-page';

interface Fixtures {
  loginPage: LoginPage;
  projectsPage: ProjectsPage;
  projectPage: ProjectPage;
}

export const test = base.extend<Fixtures>({
  loginPage: async ({ page }, use) => await use(new LoginPage(page)),
  projectsPage: async ({ page }, use) => await use(new ProjectsPage(page)),
  projectPage: async ({ page }, use) => await use(new ProjectPage(page)),
});
