# References

## Source Files Modified

| File | Purpose |
|---|---|
| `apps/web/src/main.tsx` | App entry point — added `ConfigProvider` wrapper |
| `apps/web/src/features/auth/LoginPage.tsx` | Login form — migrated to neat-design `Form`, `Input`, `Button`, `Alert` |
| `apps/web/src/features/auth/TenantPickerPage.tsx` | Tenant selector — migrated to neat-design `Button` list |
| `apps/web/src/App.tsx` | App routes + Dashboard placeholder — migrated to neat-design `Typography` |
| `apps/web/src/features/auth/PrivateRoute.tsx` | Auth guard — replaced loading div with neat-design `Spinner` |

## Supporting Files Read

| File | Purpose |
|---|---|
| `agent-os/standards/frontend/standards.md` | Frontend coding standards and neat-design usage guidelines |
| `apps/web/package.json` | Confirmed `@derbysoft/neat-design ^2.2.3` is installed |
| `apps/web/src/features/auth/AuthContext.tsx` | (referenced indirectly via imports in migrated files) |
| `apps/web/src/lib/apiClient.ts` | (referenced via `ApiError` import in `LoginPage.tsx`) |

## Package

- `@derbysoft/neat-design ^2.2.3` — wraps Ant Design v5.26+
- Components used: `ConfigProvider`, `Form`, `Form.Item`, `Input`, `Input.Password`, `Button`, `Alert`, `Spinner`, `Typography`, `Typography.Title`, `Typography.Paragraph`
