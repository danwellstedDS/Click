# Shape Notes — Neat-Design Migration

## Problem

All existing views in `apps/web` use raw HTML elements (`<form>`, `<input>`, `<button>`, `<ul>`, `<li>`) with inline styles. The frontend standards mandate using `@derbysoft/neat-design` exclusively — no raw form elements, no inline styles where a neat-design component exists.

## Appetite

Small batch — 5 files, auth feature only. Purely a UI layer migration; no business logic changes.

## Solution

### ConfigProvider
Wrap the entire app at the `main.tsx` entry point. This enables neat-design theming and context for all descendant components.

### LoginPage
- Delegate field state to `Form` (remove per-field `useState`)
- `onFinish` receives typed `values` object instead of a `FormEvent`
- `Input.Password` handles the show/hide toggle
- `Alert` replaces the red `<p>` — semantically correct and styled
- `Button loading` prop replaces the manual disabled + text swap

### TenantPickerPage
- `Button variant="secondary" block` replaces `<button style={{width:"100%"}}>`
- Drop `<ul>/<li>` — not semantically required for an action list
- A wrapping `<div style={{ marginBottom: 8 }}>` is the minimal structural remnant (acceptable)

### App.tsx Dashboard
- `Typography.Title` and `Typography.Paragraph` replace `<h1>/<p>`
- `padding: 24` kept as structural wrapper since this is a placeholder page (no neat-design `Layout` added yet)

### PrivateRoute
- `<Spinner />` is the canonical loading indicator per the component catalogue

## Out of Scope

- Adding `Layout`, `Card`, or other structural primitives to the page wrappers (deferred to future feature pages)
- Migrating `AuthContext.tsx` or `lib/apiClient.ts` — no UI changes needed there
- Writing Cypress tests for migrated components
