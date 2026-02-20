/**
 * E2E smoke tests for the auth flow.
 *
 * NOT implemented in this task. Requires Playwright setup.
 *
 * Setup instructions:
 *   npm install -D @playwright/test
 *   npx playwright install
 *
 * Planned test cases:
 *   1. Navigate to / — redirected to /login
 *   2. Submit invalid credentials — error message shown
 *   3. Submit valid credentials — redirected to /
 *   4. Reload page — user stays authenticated (session restored via /me)
 *   5. Multi-tenant user — redirected to /select-tenant, select one, redirected to /
 *   6. Logout — redirected to /login, subsequent / redirects to /login
 */

// TODO: implement once Playwright is configured
