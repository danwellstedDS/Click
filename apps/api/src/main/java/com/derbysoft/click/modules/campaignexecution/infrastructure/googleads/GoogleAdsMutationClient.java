package com.derbysoft.click.modules.campaignexecution.infrastructure.googleads;

import com.derbysoft.click.modules.campaignexecution.application.ports.GoogleAdsMutationPort;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * MVP stub: logs mutation intent and returns success. Real Google Ads API calls deferred to Phase 2.
 */
@Component
public class GoogleAdsMutationClient implements GoogleAdsMutationPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleAdsMutationClient.class);

    @Override
    public MutationResult createCampaign(String customerId, String managerId, CampaignSpec spec) {
        log.info("[STUB] createCampaign customerId={} payload={}", customerId, spec.payload());
        return success("campaigns/stub-" + UUID.randomUUID());
    }

    @Override
    public MutationResult updateCampaign(String customerId, String managerId, CampaignSpec spec) {
        log.info("[STUB] updateCampaign customerId={} resourceId={}", customerId, spec.resourceId());
        return success(spec.resourceId());
    }

    @Override
    public MutationResult createAdGroup(String customerId, String managerId, AdGroupSpec spec) {
        log.info("[STUB] createAdGroup customerId={} campaignId={}", customerId, spec.campaignId());
        return success("adGroups/stub-" + UUID.randomUUID());
    }

    @Override
    public MutationResult updateAdGroup(String customerId, String managerId, AdGroupSpec spec) {
        log.info("[STUB] updateAdGroup customerId={} resourceId={}", customerId, spec.resourceId());
        return success(spec.resourceId());
    }

    @Override
    public MutationResult createAd(String customerId, String managerId, AdSpec spec) {
        log.info("[STUB] createAd customerId={} adGroupId={}", customerId, spec.adGroupId());
        return success("ads/stub-" + UUID.randomUUID());
    }

    @Override
    public MutationResult updateAd(String customerId, String managerId, AdSpec spec) {
        log.info("[STUB] updateAd customerId={} resourceId={}", customerId, spec.resourceId());
        return success(spec.resourceId());
    }

    @Override
    public MutationResult createKeyword(String customerId, String managerId, KeywordSpec spec) {
        log.info("[STUB] createKeyword customerId={} adGroupId={}", customerId, spec.adGroupId());
        return success("keywords/stub-" + UUID.randomUUID());
    }

    @Override
    public MutationResult updateKeyword(String customerId, String managerId, KeywordSpec spec) {
        log.info("[STUB] updateKeyword customerId={} resourceId={}", customerId, spec.resourceId());
        return success(spec.resourceId());
    }

    private MutationResult success(String resourceId) {
        return new MutationResult(true, resourceId, null, null);
    }
}
