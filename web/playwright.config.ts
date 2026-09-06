import { defineConfig, devices } from "@playwright/test";

const WEB_PORT = 5500;
const API_PORT = 4500;

export default defineConfig({
  testDir: "./e2e",
  testMatch: "**/*.spec.ts",
  fullyParallel: false,
  retries: 0,
  reporter: "list",
  globalSetup: "./e2e/global-setup.ts",
  globalTeardown: "./e2e/global-teardown.ts",
  use: {
    baseURL: `http://localhost:${WEB_PORT}`,
    ...devices["iPhone 13"],
    defaultBrowserType: "chromium",
  },
  projects: [{ name: "mobile-chromium", use: { browserName: "chromium" } }],
  webServer: {
    command: `npx vite --port ${WEB_PORT}`,
    url: `http://localhost:${WEB_PORT}`,
    reuseExistingServer: false,
    env: { VITE_API_URL: `http://localhost:${API_PORT}` },
    stdout: "pipe",
    stderr: "pipe",
  },
});
