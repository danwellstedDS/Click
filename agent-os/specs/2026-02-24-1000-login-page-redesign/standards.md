# Login Page Redesign — Standards

## Component Standards

- Use `@derbysoft/neat-design` components (`Form`, `Input`, `Button`, `Alert`) — do not introduce external UI libraries
- Inline styles are acceptable for one-off layout; no new CSS files required
- SVG logo imported as a module (`import logo from "../../assets/logo.svg"`) — Vite handles this natively

## Styling

- Brand dark color: `#00131C`
- Link blue: `#1677ff` (Ant Design default)
- Background: white `#fff`
- Decorative blobs use `radial-gradient` — no additional dependencies

## Accessibility

- Logo has descriptive `alt="DerbySoft"`
- Form fields retain label associations via `Form.Item name` prop
- "Forgot Password?" is an `<a>` (keyboard-focusable) even though non-functional

## Security

- No credentials logged or persisted beyond existing `useAuth` implementation
- "Forgot Password?" link uses `e.preventDefault()` — no navigation or network request
