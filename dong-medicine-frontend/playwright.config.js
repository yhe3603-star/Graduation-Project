import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: process.env.CI ? 'github' : 'html',
  expect: {
    timeout: 10000,
  },
  use: {
    baseURL: process.env.BASE_URL || (process.env.CI ? 'http://localhost:3000' : 'http://localhost'),
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 10000,
    navigationTimeout: 15000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
  webServer: process.env.CI ? {
    // CI workflow already builds the frontend; just serve the pre-built dist
    command: 'npx serve@latest dist -l 3000 --single --no-clipboard',
    port: 3000,
    reuseExistingServer: true,
  } : undefined,
})
