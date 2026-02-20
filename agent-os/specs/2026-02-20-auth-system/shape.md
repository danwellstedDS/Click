# Auth System — Shaping Notes

## Scope
Email/password login, JWT (access + refresh), multi-tenant users, ADMIN/VIEWER roles,
/login page in React frontend.

## Key Decisions
- Users are global (email globally unique). tenant_memberships maps user→tenant with role.
- Internal users can belong to multiple tenants; login issues JWT for first tenant, switch-tenant re-issues.
- Refresh tokens: 7-day, stored in DB (refresh_tokens table), sent as httpOnly cookie.
- Access tokens: 8 hours, HS256, JWT_SECRET env var.
- Password hashing: bcrypt cost 12 (at.favre.lib:bcrypt:0.10.2).
- Token storage: httpOnly cookies (auth_token + refresh_token). Secure flag conditional on ENV=production.
- Frontend routing: react-router-dom v6.
- AuthClaims kept pure in domain layer; api.AuthPrincipal wraps it as Ktor Principal.
- UserRepository uses a port interface (UserRepositoryPort) to enable contract tests without DB.

## Visuals
None — design system to be added later.

## References
No existing reference implementations (greenfield).

## Product Alignment
Foundational feature for multi-tenant hotel chain marketing platform.
tenantId in every JWT claim enforces isolation per the auth/authorization-multitenancy standard.
