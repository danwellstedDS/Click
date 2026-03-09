# BC6 — Google Search Ads Management (Execution Objects)

See the full plan in the conversation history. This spec covers the Click-side intended state
for Google Search campaign structures (`CampaignPlan`) and asynchronous structural writes to
Google Ads, with publish gate (draft → publish → apply), partial-apply semantics, item-level
lifecycle, execution incident tracking, and drift detection.

MVP: structural writes (campaign / ad group / ad / keyword CREATE + UPDATE). Deletes are Phase 2.
