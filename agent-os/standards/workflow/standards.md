Git workflow spec (Agent OS) for full-stack apps (frontend + backend)
1) Repository + branch model
Default branches

main: always releasable; protected; no direct pushes.
develop (optional): only if you need a shared integration branch. If not, merge directly to main via PR.
Trunk-based with short-lived branches

Work happens on branches off main (or develop if you use it).
Branches should be short-lived (hours/days), frequently rebased/updated from base.
Branch protection (recommended)
Require PR to merge.
Require CI green (backend + frontend).
Require at least 1 review (2 for risky changes).
Require linear history (squash or rebase merge).

Disallow force-pushes on main (and develop if used).


2) Branch naming convention
Format
<type>/<scope>-<short-slug>

type: one of the allowed types below
scope: fe, be, full, infra, docs, ci, api, db (pick a small fixed set)
short-slug: lowercase, hyphen-separated, 3–8 words max
Allowed types

feature/ – new user-facing functionality
fix/ – bug fix
chore/ – non-feature maintenance (deps, formatting, cleanup)
refactor/ – behavior-preserving code restructure
test/ – adding/improving tests without production changes
perf/ – performance improvements
docs/ – documentation only
ci/ – pipeline/build tooling changes
release/ – release prep (version bumps, changelog, tags)
hotfix/ – urgent production fix (if you use release branches)
Examples

feature/fe-checkout-form-validation
feature/be-create-booking-endpoint
fix/full-null-user-session
refactor/be-split-pricing-service
chore/infra-upgrade-node-20
test/be-add-booking-service-cases
ci/full-parallelize-tests


3) Commit specs (incremental + TDD-friendly)
General rules
• Commits must be:
Small, coherent, and reversible
Buildable at each step (or at least locally runnable)
With clear intent in the message
• Avoid “mega commits” and avoid mixing unrelated changes.
• Prefer multiple commits over one commit that does everything.
Commit message format (Conventional Commits)
<type>(<scope>): <summary>

type: feat, fix, chore, refactor, test, docs, ci, perf, build
scope: fe, be, api, db, full, etc.
Examples

test(be): add failing spec for booking overlap
feat(be): implement booking overlap validation
fix(fe): prevent double submit on checkout
refactor(full): extract shared money formatter
chore(ci): cache pnpm store


4) TDD commit protocol (commit each time a broken test is fixed)
This is the key behavior to enforce for Agent OS.
Red → Green → Refactor commit cadence
1. RED commit (introduce failing test)

Add a single test (or a small batch) describing one behavior.
It should fail for the right reason.
No production code yet (or minimal scaffolding if absolutely required).
Commit message

test(<scope>): add failing test for <behavior> [RED]
2. GREEN commit (make the test pass)

Minimal implementation to pass the failing test(s).
Keep the change set focused on what’s required to go green.
Commit message
• feat(<scope>): make <behavior> pass [GREEN]
If it’s a bug, use fix(<scope>): ... [GREEN]
3. REFACTOR commit (clean up without changing behavior)

Refactor code, remove duplication, rename, improve structure.
Tests remain green.
Commit message

refactor(<scope>): <refactor summary> [REFACTOR]
Important constraint
• If multiple tests are failing due to one change:
Prefer isolating them into separate cycles if possible.
If they must be fixed together, the GREEN commit can cover all, but keep it narrowly scoped.
When to squash

Prefer not squashing during development because the point is incremental history.
If the repo enforces squash merges on PRs, keep the incremental commits in the branch anyway; the PR description should summarize the sequence. If you want to preserve the sequence, use rebase merge instead of squash.


5) Full-stack change coordination (frontend + backend)
Preferred approach: vertical slices
• Implement a feature end-to-end in small slices:
1. API contract + backend failing test (RED)
2. backend implementation (GREEN)
3. minimal FE integration (RED if FE tests exist)
4. FE implementation (GREEN)
5. refactor (REFACTOR)
API contracts
• Keep an explicit place for contracts:
openapi/ or contracts/ directory (OpenAPI, JSON Schema, or TS types)
• Any contract change should include:
backend tests updated/added
frontend types regenerated or updated (commit separately if heavy)
Commit scoping
Avoid mixing FE and BE in one commit unless it’s a small atomic vertical slice.

If you touch both, use scope=full.


6) PR specs
PR title
• Should match branch intent; use Conventional Commit style:
feat(be): booking overlap validation
fix(full): session refresh regression
PR description template

What / Why
How tested (commands)
Screenshots for FE
Risk notes / rollout notes
Follow-ups (explicitly listed)
PR size guideline

Prefer < 400 changed lines; split larger work into stacked PRs.


7) Merge & release rules
Merge
• Rebase (or merge) main into branch frequently.
• Merge strategy:
Prefer rebase merge if you want to preserve incremental commit history.
Prefer squash merge if you want clean main history (but you lose the TDD commit cadence on main).
Release tagging
Tag releases from main: vMAJOR.MINOR.PATCH

Use release/ branches only if you need stabilization cycles.


8) Required checks (CI gates)
Minimum CI
• Backend:
unit tests
lint
typecheck
• Frontend:
unit tests
lint
typecheck
build
• Optional:
integration tests
e2e (Playwright/Cypress)
contract tests (OpenAPI validation)

9) Agent OS “rules of operation” (actionable constraints)
1. Always create a branch using the naming scheme before coding.
2. Work in TDD cycles:
commit failing test [RED]
commit minimal fix [GREEN] each time a broken test becomes green
commit cleanup [REFACTOR] separately
3. Never commit unrelated changes in the same commit.
4. Never merge with failing CI.
5. Prefer vertical slices; keep FE and BE changes separable where possible.
6. Keep PRs small; if too big, split into stacked PRs.

If you want, I can output this as a CONTRIBUTING.md section plus a git config/hooks suggestion (commit-msg hook to enforce prefixes and [RED]/[GREEN]/[REFACTOR]).