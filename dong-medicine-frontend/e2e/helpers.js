/**
 * E2E tests that call backend APIs need a running backend server.
 * In CI, only the static frontend is built and served — no backend.
 * Use this helper to skip such tests gracefully in CI.
 */
export const backendAvailable = !process.env.CI
