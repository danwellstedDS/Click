# Login Page Redesign — Shape

## Layout

Full-viewport wrapper:
- `min-height: 100vh`
- `position: relative`
- `overflow: hidden`
- `background: #fff`

Content column:
- `max-width: 400px`
- `margin: 0 auto`
- `padding: 80px 24px 24px`
- `position: relative; z-index: 1`

## Elements (top to bottom)

1. **Logo** — `logo.svg`, height 32px, margin-bottom 24px
2. **Heading** — "Sign In", 28px, weight 600, color `#00131C`, margin-bottom 24px
3. **Work Email field** — Ant Design `Form.Item` with `Input`, required + email validation
4. **Password field** — Ant Design `Form.Item` with `Input.Password`, required
5. **Forgot Password?** — right-aligned `<a>` link, non-functional (`e.preventDefault()`)
6. **Error alert** — conditional `Alert` (type=error), shown on login failure
7. **Sign In button** — full-width primary `Button`, shows loading state

## Decorative Background

Two absolutely-positioned blobs, `z-index: 0`, `pointer-events: none`:

| Blob | Position | Size | Color |
|------|----------|------|-------|
| 1 | bottom: -120, right: -80 | 420×420 | teal `rgba(0,224,225,0.35)` |
| 2 | bottom: -60, right: 60 | 280×280 | indigo `rgba(99,102,241,0.2)` |
