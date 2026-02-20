# Neat-Design Migration — All Existing Views

## Context

The `apps/web` project already has `@derbysoft/neat-design ^2.2.3` installed but none of the existing views use it. All current views use raw HTML elements with inline styles. The goal is to replace every raw HTML element and inline style with the appropriate neat-design component, and add the required `ConfigProvider` wrapper to `main.tsx`.

**Scope:** 5 files, auth feature only (the only feature currently built).

---

## Spec Folder

`agent-os/specs/2026-02-20-1500-neat-design-migration/`

---

## Files to Modify

| File | Change |
|---|---|
| `apps/web/src/main.tsx` | Add `ConfigProvider` wrapper |
| `apps/web/src/features/auth/LoginPage.tsx` | Replace raw form with `Form`, `Form.Item`, `Input`, `Button`, `Alert` |
| `apps/web/src/features/auth/TenantPickerPage.tsx` | Replace `ul/li/button` with neat-design `Button` list, remove inline styles |
| `apps/web/src/App.tsx` | Replace `Dashboard` inline div with neat-design `Typography` |
| `apps/web/src/features/auth/PrivateRoute.tsx` | Replace `<div>Loading...</div>` with `Spinner` |

---

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-02-20-1500-neat-design-migration/` containing:

- **plan.md** — this full plan
- **shape.md** — shaping notes
- **standards.md** — full content of `agent-os/standards/frontend/standards.md`
- **references.md** — pointers to the source files studied

---

## Task 2: Add ConfigProvider to `main.tsx`

**File:** `apps/web/src/main.tsx`

**Current:**
```tsx
<BrowserRouter>
  <AuthProvider>
    <App />
  </AuthProvider>
</BrowserRouter>
```

**Replace with:**
```tsx
import { ConfigProvider } from '@derbysoft/neat-design';

<BrowserRouter>
  <ConfigProvider>
    <AuthProvider>
      <App />
    </AuthProvider>
  </ConfigProvider>
</BrowserRouter>
```

---

## Task 3: Migrate `LoginPage.tsx`

**File:** `apps/web/src/features/auth/LoginPage.tsx`

**Key changes:**
- Remove per-field `useState` for `email` and `password` — let neat-design `Form` manage field state
- Change `handleSubmit` signature from `(e: React.FormEvent)` to `(values: { email: string; password: string })`
- Replace outer `<div style={...}>` with neat-design layout (centered card pattern)
- Replace `<form onSubmit>` with `<Form onFinish={handleSubmit} layout="vertical">`
- Replace each `<div><label><input>` group with `<Form.Item name="..." label="..." rules={[{required:true}]}><Input /></Form.Item>`
- Replace password `<input type="password">` with `<Input.Password />`
- Replace `<p style={{color:'red'}}>` error with `<Alert type="error" message={error} showIcon />`
- Replace `<button disabled={isSubmitting}>` with `<Button variant="primary" htmlType="submit" loading={isSubmitting}>Sign in</Button>`
- Remove all inline `style` props

**Result shape:**
```tsx
import { Form, Input, Button, Alert } from '@derbysoft/neat-design';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(values: { email: string; password: string }) {
    setError(null);
    setIsSubmitting(true);
    try {
      const tenants = await login(values.email, values.password);
      navigate(tenants.length > 1 ? '/select-tenant' : '/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'An unexpected error occurred. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    // centered layout wrapper
    <Form onFinish={handleSubmit} layout="vertical">
      <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
        <Input />
      </Form.Item>
      <Form.Item name="password" label="Password" rules={[{ required: true }]}>
        <Input.Password />
      </Form.Item>
      {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />}
      <Form.Item>
        <Button variant="primary" htmlType="submit" loading={isSubmitting} block>
          Sign in
        </Button>
      </Form.Item>
    </Form>
  );
}
```

---

## Task 4: Migrate `TenantPickerPage.tsx`

**File:** `apps/web/src/features/auth/TenantPickerPage.tsx`

**Key changes:**
- Remove outer `<div style={...}>` — replace with neat-design layout wrapper
- Remove `<ul>`, `<li>` — replace with a list of neat-design `Button` components (full-width, `variant="secondary"`)
- Remove all inline `style` props

**Result shape:**
```tsx
import { Button } from '@derbysoft/neat-design';

export function TenantPickerPage() {
  // ... same logic ...
  return (
    // centered layout wrapper
    <>
      {tenants.map((t) => (
        <Button
          key={t.tenantId}
          variant="secondary"
          block
          onClick={() => handleSelect(t.tenantId)}
        >
          {t.tenantId} ({t.role})
        </Button>
      ))}
    </>
  );
}
```

---

## Task 5: Migrate `App.tsx` Dashboard

**File:** `apps/web/src/App.tsx`

**Key changes:**
- Replace the inline-styled `Dashboard` function's `<div style={{fontFamily,padding}}>` with neat-design `Typography` components (`Typography.Title`, `Typography.Paragraph`) or a simple `Layout.Content` wrapper
- Remove all inline `style` props

**Result shape:**
```tsx
import { Typography } from '@derbysoft/neat-design';

function Dashboard() {
  return (
    <div style={{ padding: 24 }}>
      <Typography.Title level={1}>Dashboard</Typography.Title>
      <Typography.Paragraph>Welcome! You are authenticated.</Typography.Paragraph>
    </div>
  );
}
```

Note: `padding: 24` may remain if no neat-design layout wrapper is added here — acceptable since this is a placeholder page.

---

## Task 6: Migrate `PrivateRoute.tsx`

**File:** `apps/web/src/features/auth/PrivateRoute.tsx`

**Key change:**
- Replace `<div>Loading...</div>` with `<Spinner />` from neat-design

```tsx
import { Spinner } from '@derbysoft/neat-design';

if (isLoading) return <Spinner />;
```

---

## Verification

After all tasks:
1. `npm run dev` in `apps/web` — app loads without errors
2. `/login` route renders the neat-design Form (email + password fields, Sign in button)
3. Failed login shows an `Alert` (red) instead of a red `<p>`
4. Submitting the form shows the `loading` spinner on the Button
5. `/select-tenant` route renders neat-design Buttons for each tenant
6. `PrivateRoute` shows `Spinner` while auth is loading
7. No raw `<input>`, `<button>`, `<form>`, or inline `style` props remain in the migrated files (except acceptable structural wrappers)
