# Login Page Redesign — Plan

## Date
2026-02-24

## Goal
Redesign `LoginPage.tsx` to match the DerbySoft SSO reference design (`sso.derbysoftsec.com`), adding branding, visual polish, and a decorative background.

## Tasks

- [x] Create spec documentation (plan.md, shape.md, standards.md, references.md)
- [x] Redesign `apps/web/src/features/auth/LoginPage.tsx`

## Changes Made

### `LoginPage.tsx`
- Added DerbySoft logo (`../../assets/logo.svg`) at top, centered in column
- Added "Sign In" `<h1>` heading
- Changed email label from `"Email"` → `"Work Email"`
- Added non-functional "Forgot Password?" link (right-aligned, below password field)
- Added full-viewport wrapper with white background
- Added two decorative radial-gradient blobs (teal + indigo) positioned bottom-right

## Out of Scope
- Google Sign-In
- Phone Number tab
- Create Account link
