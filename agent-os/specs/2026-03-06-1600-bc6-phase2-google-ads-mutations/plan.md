# BC6 Phase 2 — Real Google Ads Mutations

## Scope

Replace the `GoogleAdsMutationClient` stub with real Google Ads API v23 calls.
Add `targetCustomerId` to `CampaignPlan` and `WriteAction` so every plan is explicitly
scoped to a customer account. Fix `WriteActionExecutor.resolveCustomerId()` bug.

## Tasks

1. Create spec documentation (this directory)
2. Update `docs/bd.md` — add Phase 2 locked decisions under BC6
3. DB migration `V202603060007` — add `google_ads_customer_id` to `campaign_plans`, `target_customer_id` to `write_actions`
4. Domain — `CampaignPlan.targetCustomerId`, `WriteAction.targetCustomerId`
5. Infrastructure — JPA entities + mappers for new fields
6. Application — thread `targetCustomerId` through `CampaignPlanService`, `PlanApplyService`, `WriteActionExecutor`
7. HTTP layer — `CreateCampaignPlanRequest`, `CampaignPlanResponse`, `CampaignPlanController`
8. Replace `GoogleAdsMutationClient` stub with real Google Ads API calls
9. Update `RetryPolicyEngine` to handle `MutationApiException`
10. Tests — `GoogleAdsMutationClientTest`, update `RetryPolicyEngineTest`, `CampaignPlanControllerTest`, `CampaignPlanServiceTest`
