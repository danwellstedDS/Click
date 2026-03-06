package com.derbysoft.click.modules.campaignexecution.application.ports;

public interface GoogleAdsMutationPort {

    MutationResult createCampaign(String customerId, String managerId, CampaignSpec spec);
    MutationResult updateCampaign(String customerId, String managerId, CampaignSpec spec);
    MutationResult createAdGroup(String customerId, String managerId, AdGroupSpec spec);
    MutationResult updateAdGroup(String customerId, String managerId, AdGroupSpec spec);
    MutationResult createAd(String customerId, String managerId, AdSpec spec);
    MutationResult updateAd(String customerId, String managerId, AdSpec spec);
    MutationResult createKeyword(String customerId, String managerId, KeywordSpec spec);
    MutationResult updateKeyword(String customerId, String managerId, KeywordSpec spec);

    record CampaignSpec(String resourceId, String payload) {}
    record AdGroupSpec(String resourceId, String campaignId, String payload) {}
    record AdSpec(String resourceId, String adGroupId, String payload) {}
    record KeywordSpec(String resourceId, String adGroupId, String payload) {}

    record MutationResult(boolean success, String resourceId,
                           String failureClass, String failureReason) {}
}
