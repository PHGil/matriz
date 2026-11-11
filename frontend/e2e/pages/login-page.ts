import { expect, type Locator, type Page } from '@playwright/test';
import { DISPLAY_NAME, DemoUser, demoPassword } from '../test-data/users';

// The login form belongs to Keycloak (realm "matriz"); the app redirects there on first load.
export class LoginPage {
  readonly username: Locator;
  readonly password: Locator;
  readonly signIn: Locator;

  constructor(readonly page: Page) {
    this.username = page.getByRole('textbox', { name: 'Username' });
    this.password = page.getByRole('textbox', { name: 'Password' });
    this.signIn = page.getByRole('button', { name: 'Sign In' });
  }

  goto() {
    return this.page.goto('/');
  }

  async login(user: DemoUser) {
    await this.username.fill(user);
    await this.password.fill(demoPassword());
    await this.signIn.click();
  }

  async checkLoggedIn(user: DemoUser) {
    await expect(this.page).toHaveURL(/localhost:4200/);
    await expect(this.page.getByRole('button', { name: DISPLAY_NAME[user] })).toBeVisible();
  }
}
