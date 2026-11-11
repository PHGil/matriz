import { expect, type Locator, type Page } from '@playwright/test';

export class ProjectPage {
  readonly newTaskButton: Locator;
  readonly titleInput: Locator;
  readonly submitButton: Locator;
  readonly notification: Locator;

  constructor(readonly page: Page) {
    this.newTaskButton = page.getByRole('button', { name: 'Nova tarefa' });
    this.titleInput = page.getByLabel('Título');
    this.submitButton = page.getByRole('button', { name: 'Criar tarefa' });
    this.notification = page.getByRole('status');
  }

  async checkPage(projectName: string) {
    await expect(this.page.getByRole('heading', { level: 1, name: projectName })).toBeVisible();
  }

  async createTask(title: string) {
    await this.newTaskButton.click();
    await this.titleInput.fill(title);
    await this.submitButton.click();
  }

  async checkTaskCreated(title: string) {
    await expect(this.notification).toContainText(`Tarefa "${title}" criada.`);
  }

  checkTitleError() {
    return expect(this.page.getByText('O título deve ter entre 3 e 150 caracteres.')).toBeVisible();
  }

  // Each status column is a labelled section of the board.
  column(name: string) {
    return this.page.getByRole('region', { name, exact: true });
  }

  async checkTaskInColumn(title: string, column: string) {
    await expect(this.column(column).getByRole('link', { name: title, exact: true })).toBeVisible();
  }

  async checkColumnCount(column: string, count: number) {
    await expect(this.column(column).getByRole('heading', { level: 3 })).toContainText(`${count}`);
  }
}
