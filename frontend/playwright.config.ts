import { defineConfig, devices } from '@playwright/test';

// Needs the infrastructure (docker compose up -d) and the backend with profile "st" (database matriz_st) on :8080.
export default defineConfig({
  testDir: './e2e/tests',
  outputDir: './e2e/results',
  fullyParallel: false,
  forbidOnly: !!process.env['CI'],
  retries: 0,
  workers: 1,
  reporter: [['list']],

  use: {
    baseURL: 'http://localhost:4200',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },

  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],

  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    timeout: 120000,
    reuseExistingServer: true,
  },
});
