# Shape — BC6 Phase 2

## Problem

The `GoogleAdsMutationClient` is a stub returning fake resource IDs.
`WriteActionExecutor.resolveCustomerId()` returns `managerId` instead of the customer account ID.
`CampaignPlan` has no `targetCustomerId`, so there is no explicit account scoping at plan level.

## Solution

- Add `targetCustomerId` (String) to `CampaignPlan` — snapshot the target Google Ads customer account at plan creation.
- Add `targetCustomerId` to `WriteAction` — copied from `CampaignPlan` at apply time, used at execution time.
- Fix `resolveCustomerId()` to return `action.getTargetCustomerId()` (the account) while keeping `managerId` for `setLoginCustomerId()`.
- Replace stub with real Google Ads API v23 calls, mirroring `GoogleAdsReportingClient` patterns.

## No new BC

`GoogleAdsMutationPort` is the anti-corruption layer. API version lives only in `GoogleAdsMutationClient`.
A new BC would fragment BC6's cohesive capability without isolation benefit.

## Payload schema

See plan.md for full JSON schemas per mutation type.
Inner payload records are `private` in `GoogleAdsMutationClient` — no leakage to domain or port layers.
