import { expect, type Locator, type Page } from '@playwright/test';

export class ProjectsPage {
  readonly title: Locator;

  constructor(readonly page: Page) {
    this.title = page.getByRole('heading', { level: 1, name: 'Projetos' });
  }

  async goto() {
    await this.page.getByRole('link', { name: 'Projetos', exact: true }).click();
    await expect(this.title).toBeVisible();
  }

  async openProject(name: string) {
    await this.page.getByRole('link', { name, exact: true }).click();
  }
}
