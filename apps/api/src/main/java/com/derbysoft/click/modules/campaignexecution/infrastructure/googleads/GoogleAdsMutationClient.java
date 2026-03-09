package com.derbysoft.click.modules.campaignexecution.infrastructure.googleads;

import com.derbysoft.click.modules.campaignexecution.application.ports.GoogleAdsMutationPort;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.googleads.GoogleAdsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v23.common.AdTextAsset;
import com.google.ads.googleads.v23.common.KeywordInfo;
import com.google.ads.googleads.v23.common.ManualCpc;
import com.google.ads.googleads.v23.common.ResponsiveSearchAdInfo;
import com.google.ads.googleads.v23.enums.AdGroupAdStatusEnum.AdGroupAdStatus;
import com.google.ads.googleads.v23.enums.AdGroupStatusEnum.AdGroupStatus;
import com.google.ads.googleads.v23.enums.AdvertisingChannelTypeEnum.AdvertisingChannelType;
import com.google.ads.googleads.v23.enums.BiddingStrategyTypeEnum.BiddingStrategyType;
import com.google.ads.googleads.v23.enums.BudgetDeliveryMethodEnum.BudgetDeliveryMethod;
import com.google.ads.googleads.v23.enums.CampaignStatusEnum.CampaignStatus;
import com.google.ads.googleads.v23.enums.AdGroupCriterionStatusEnum.AdGroupCriterionStatus;
import com.google.ads.googleads.v23.enums.EuPoliticalAdvertisingStatusEnum.EuPoliticalAdvertisingStatus;
import com.google.ads.googleads.v23.enums.KeywordMatchTypeEnum.KeywordMatchType;
import com.google.ads.googleads.v23.errors.GoogleAdsError;
import com.google.ads.googleads.v23.errors.GoogleAdsException;
import com.google.ads.googleads.v23.resources.Ad;
import com.google.ads.googleads.v23.resources.AdGroup;
import com.google.ads.googleads.v23.resources.AdGroupAd;
import com.google.ads.googleads.v23.resources.AdGroupCriterion;
import com.google.ads.googleads.v23.resources.Campaign;
import com.google.ads.googleads.v23.resources.CampaignBudget;
import com.google.ads.googleads.v23.services.AdGroupAdOperation;
import com.google.ads.googleads.v23.services.AdGroupAdServiceClient;
import com.google.ads.googleads.v23.services.AdGroupCriterionOperation;
import com.google.ads.googleads.v23.services.AdGroupCriterionServiceClient;
import com.google.ads.googleads.v23.services.AdGroupOperation;
import com.google.ads.googleads.v23.services.AdGroupServiceClient;
import com.google.ads.googleads.v23.services.CampaignBudgetOperation;
import com.google.ads.googleads.v23.services.CampaignBudgetServiceClient;
import com.google.ads.googleads.v23.services.CampaignOperation;
import com.google.ads.googleads.v23.services.CampaignServiceClient;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.FieldMask;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GoogleAdsMutationClient implements GoogleAdsMutationPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleAdsMutationClient.class);
    private static final String ADS_SCOPE = "https://www.googleapis.com/auth/adwords";

    private final GoogleAdsConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleAdsMutationClient(GoogleAdsConfig config) {
        this.config = config;
    }

    // ── Campaign ─────────────────────────────────────────────────────────────

    @Override
    public MutationResult createCampaign(String customerId, String managerId, CampaignSpec spec) {
        try {
            CampaignPayload payload = parse(spec.payload(), CampaignPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            String budgetResourceName;
            try (CampaignBudgetServiceClient budgetClient =
                     client.getLatestVersion().createCampaignBudgetServiceClient()) {
                CampaignBudget budget = CampaignBudget.newBuilder()
                    .setName(payload.name() + " Budget " + System.currentTimeMillis())
                    .setAmountMicros(payload.budgetAmountMicros())
                    .setDeliveryMethod(BudgetDeliveryMethod.STANDARD)
                    .build();
                CampaignBudgetOperation budgetOp = CampaignBudgetOperation.newBuilder()
                    .setCreate(budget).build();
                budgetResourceName = budgetClient.mutateCampaignBudgets(
                    strippedId, List.of(budgetOp)).getResults(0).getResourceName();
            }

            try (CampaignServiceClient campaignClient =
                     client.getLatestVersion().createCampaignServiceClient()) {
                Campaign.Builder campaignBuilder = Campaign.newBuilder()
                    .setName(payload.name())
                    .setCampaignBudget(budgetResourceName)
                    .setAdvertisingChannelType(AdvertisingChannelType.valueOf(payload.advertisingChannelType()))
                    .setStatus(CampaignStatus.valueOf(payload.status()))
                    .setManualCpc(ManualCpc.newBuilder().setEnhancedCpcEnabled(false).build())
                    .setContainsEuPoliticalAdvertising(EuPoliticalAdvertisingStatus.DOES_NOT_CONTAIN_EU_POLITICAL_ADVERTISING);
                if (payload.startDate() != null) campaignBuilder.setStartDateTime(payload.startDate());
                if (payload.endDate() != null) campaignBuilder.setEndDateTime(payload.endDate());

                CampaignOperation op = CampaignOperation.newBuilder()
                    .setCreate(campaignBuilder.build()).build();
                String resourceName = campaignClient.mutateCampaigns(
                    strippedId, List.of(op)).getResults(0).getResourceName();
                log.info("createCampaign customerId={} resourceName={}", customerId, resourceName);
                return new MutationResult(true, resourceName, null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    @Override
    public MutationResult updateCampaign(String customerId, String managerId, CampaignSpec spec) {
        try {
            CampaignPayload payload = parse(spec.payload(), CampaignPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            try (CampaignServiceClient campaignClient =
                     client.getLatestVersion().createCampaignServiceClient()) {
                Campaign.Builder campaignBuilder = Campaign.newBuilder()
                    .setResourceName(spec.resourceId())
                    .setName(payload.name())
                    .setStatus(CampaignStatus.valueOf(payload.status()));
                if (payload.startDate() != null) campaignBuilder.setStartDateTime(payload.startDate());
                if (payload.endDate() != null) campaignBuilder.setEndDateTime(payload.endDate());

                FieldMask.Builder maskBuilder = FieldMask.newBuilder()
                    .addPaths("name")
                    .addPaths("status");
                if (payload.startDate() != null) maskBuilder.addPaths("start_date_time");
                if (payload.endDate() != null) maskBuilder.addPaths("end_date_time");

                CampaignOperation op = CampaignOperation.newBuilder()
                    .setUpdate(campaignBuilder.build())
                    .setUpdateMask(maskBuilder.build())
                    .build();
                campaignClient.mutateCampaigns(strippedId, List.of(op));
                log.info("updateCampaign customerId={} resourceId={}", customerId, spec.resourceId());
                return new MutationResult(true, spec.resourceId(), null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    // ── AdGroup ──────────────────────────────────────────────────────────────

    @Override
    public MutationResult createAdGroup(String customerId, String managerId, AdGroupSpec spec) {
        try {
            AdGroupPayload payload = parse(spec.payload(), AdGroupPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            try (AdGroupServiceClient adGroupClient =
                     client.getLatestVersion().createAdGroupServiceClient()) {
                AdGroup adGroup = AdGroup.newBuilder()
                    .setName(payload.name())
                    .setCampaign(spec.campaignId())
                    .setStatus(AdGroupStatus.valueOf(payload.status()))
                    .setCpcBidMicros(payload.cpcBidMicros())
                    .build();
                AdGroupOperation op = AdGroupOperation.newBuilder().setCreate(adGroup).build();
                String resourceName = adGroupClient.mutateAdGroups(
                    strippedId, List.of(op)).getResults(0).getResourceName();
                log.info("createAdGroup customerId={} resourceName={}", customerId, resourceName);
                return new MutationResult(true, resourceName, null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    @Override
    public MutationResult updateAdGroup(String customerId, String managerId, AdGroupSpec spec) {
        try {
            AdGroupPayload payload = parse(spec.payload(), AdGroupPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            try (AdGroupServiceClient adGroupClient =
                     client.getLatestVersion().createAdGroupServiceClient()) {
                AdGroup adGroup = AdGroup.newBuilder()
                    .setResourceName(spec.resourceId())
                    .setName(payload.name())
                    .setStatus(AdGroupStatus.valueOf(payload.status()))
                    .setCpcBidMicros(payload.cpcBidMicros())
                    .build();
                FieldMask updateMask = FieldMask.newBuilder()
                    .addPaths("name")
                    .addPaths("status")
                    .addPaths("cpc_bid_micros")
                    .build();
                AdGroupOperation op = AdGroupOperation.newBuilder()
                    .setUpdate(adGroup)
                    .setUpdateMask(updateMask)
                    .build();
                adGroupClient.mutateAdGroups(strippedId, List.of(op));
                log.info("updateAdGroup customerId={} resourceId={}", customerId, spec.resourceId());
                return new MutationResult(true, spec.resourceId(), null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    // ── Ad ───────────────────────────────────────────────────────────────────

    @Override
    public MutationResult createAd(String customerId, String managerId, AdSpec spec) {
        try {
            AdPayload payload = parse(spec.payload(), AdPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            try (AdGroupAdServiceClient adGroupAdClient =
                     client.getLatestVersion().createAdGroupAdServiceClient()) {
                ResponsiveSearchAdInfo.Builder rsaBuilder = ResponsiveSearchAdInfo.newBuilder();
                payload.headlines().forEach(h ->
                    rsaBuilder.addHeadlines(AdTextAsset.newBuilder().setText(h.text())));
                payload.descriptions().forEach(d ->
                    rsaBuilder.addDescriptions(AdTextAsset.newBuilder().setText(d.text())));

                Ad ad = Ad.newBuilder()
                    .setResponsiveSearchAd(rsaBuilder.build())
                    .addAllFinalUrls(payload.finalUrls())
                    .build();
                AdGroupAd adGroupAd = AdGroupAd.newBuilder()
                    .setAdGroup(spec.adGroupId())
                    .setAd(ad)
                    .build();
                AdGroupAdOperation op = AdGroupAdOperation.newBuilder().setCreate(adGroupAd).build();
                String resourceName = adGroupAdClient.mutateAdGroupAds(
                    strippedId, List.of(op)).getResults(0).getResourceName();
                log.info("createAd customerId={} resourceName={}", customerId, resourceName);
                return new MutationResult(true, resourceName, null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    @Override
    public MutationResult updateAd(String customerId, String managerId, AdSpec spec) {
        try {
            AdPayload payload = parse(spec.payload(), AdPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            try (AdGroupAdServiceClient adGroupAdClient =
                     client.getLatestVersion().createAdGroupAdServiceClient()) {
                ResponsiveSearchAdInfo.Builder rsaBuilder = ResponsiveSearchAdInfo.newBuilder();
                payload.headlines().forEach(h ->
                    rsaBuilder.addHeadlines(AdTextAsset.newBuilder().setText(h.text())));
                payload.descriptions().forEach(d ->
                    rsaBuilder.addDescriptions(AdTextAsset.newBuilder().setText(d.text())));

                Ad ad = Ad.newBuilder()
                    .setResponsiveSearchAd(rsaBuilder.build())
                    .addAllFinalUrls(payload.finalUrls())
                    .build();
                AdGroupAd adGroupAd = AdGroupAd.newBuilder()
                    .setResourceName(spec.resourceId())
                    .setAd(ad)
                    .build();
                FieldMask updateMask = FieldMask.newBuilder()
                    .addPaths("ad.responsive_search_ad.headlines")
                    .addPaths("ad.responsive_search_ad.descriptions")
                    .addPaths("ad.final_urls")
                    .build();
                AdGroupAdOperation op = AdGroupAdOperation.newBuilder()
                    .setUpdate(adGroupAd)
                    .setUpdateMask(updateMask)
                    .build();
                adGroupAdClient.mutateAdGroupAds(strippedId, List.of(op));
                log.info("updateAd customerId={} resourceId={}", customerId, spec.resourceId());
                return new MutationResult(true, spec.resourceId(), null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    // ── Keyword ──────────────────────────────────────────────────────────────

    @Override
    public MutationResult createKeyword(String customerId, String managerId, KeywordSpec spec) {
        try {
            KeywordPayload payload = parse(spec.payload(), KeywordPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            try (AdGroupCriterionServiceClient criterionClient =
                     client.getLatestVersion().createAdGroupCriterionServiceClient()) {
                KeywordInfo keyword = KeywordInfo.newBuilder()
                    .setText(payload.text())
                    .setMatchType(KeywordMatchType.valueOf(payload.matchType()))
                    .build();
                AdGroupCriterion criterion = AdGroupCriterion.newBuilder()
                    .setAdGroup(spec.adGroupId())
                    .setKeyword(keyword)
                    .setCpcBidMicros(payload.cpcBidMicros())
                    .setStatus(AdGroupCriterionStatus.valueOf(payload.status()))
                    .build();
                AdGroupCriterionOperation op = AdGroupCriterionOperation.newBuilder()
                    .setCreate(criterion).build();
                String resourceName = criterionClient.mutateAdGroupCriteria(
                    strippedId, List.of(op)).getResults(0).getResourceName();
                log.info("createKeyword customerId={} resourceName={}", customerId, resourceName);
                return new MutationResult(true, resourceName, null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    @Override
    public MutationResult updateKeyword(String customerId, String managerId, KeywordSpec spec) {
        try {
            KeywordPayload payload = parse(spec.payload(), KeywordPayload.class);
            String strippedId = customerId.replace("-", "");
            GoogleAdsClient client = buildClient(managerId);

            try (AdGroupCriterionServiceClient criterionClient =
                     client.getLatestVersion().createAdGroupCriterionServiceClient()) {
                KeywordInfo keyword = KeywordInfo.newBuilder()
                    .setText(payload.text())
                    .setMatchType(KeywordMatchType.valueOf(payload.matchType()))
                    .build();
                AdGroupCriterion criterion = AdGroupCriterion.newBuilder()
                    .setResourceName(spec.resourceId())
                    .setKeyword(keyword)
                    .setCpcBidMicros(payload.cpcBidMicros())
                    .setStatus(AdGroupCriterionStatus.valueOf(payload.status()))
                    .build();
                FieldMask updateMask = FieldMask.newBuilder()
                    .addPaths("keyword.text")
                    .addPaths("keyword.match_type")
                    .addPaths("cpc_bid_micros")
                    .addPaths("status")
                    .build();
                AdGroupCriterionOperation op = AdGroupCriterionOperation.newBuilder()
                    .setUpdate(criterion)
                    .setUpdateMask(updateMask)
                    .build();
                criterionClient.mutateAdGroupCriteria(strippedId, List.of(op));
                log.info("updateKeyword customerId={} resourceId={}", customerId, spec.resourceId());
                return new MutationResult(true, spec.resourceId(), null, null);
            }
        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new MutationApiException(FailureClass.PERMANENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    // ── Infrastructure ────────────────────────────────────────────────────────

    protected GoogleAdsClient buildClient(String managerId) throws IOException {
        GoogleCredentials credentials = GoogleCredentials
            .fromStream(new FileInputStream(config.getCredentialsPath()))
            .createScoped(Collections.singletonList(ADS_SCOPE));
        long parsedManagerId = Long.parseLong(managerId.replace("-", ""));
        return GoogleAdsClient.newBuilder()
            .setCredentials(credentials)
            .setDeveloperToken(config.getDeveloperToken())
            .setLoginCustomerId(parsedManagerId)
            .build();
    }

    private void handleGoogleAdsException(GoogleAdsException e) {
        for (GoogleAdsError error : e.getGoogleAdsFailure().getErrorsList()) {
            String errorCode = error.getErrorCode().toString();
            if (errorCode.contains("OAUTH_TOKEN") ||
                errorCode.contains("NOT_AUTHORIZED") ||
                errorCode.contains("CUSTOMER_NOT_FOUND")) {
                throw new MutationAuthException("Google Ads auth failure: " + errorCode, e);
            }
        }
        throw new MutationApiException(FailureClass.PERMANENT,
            "Google Ads API error: " + e.getMessage(), e);
    }

    private void handleStatusRuntimeException(StatusRuntimeException e) {
        Status.Code code = e.getStatus().getCode();
        if (code == Status.Code.DEADLINE_EXCEEDED ||
            code == Status.Code.UNAVAILABLE ||
            code == Status.Code.RESOURCE_EXHAUSTED) {
            throw new MutationApiException(FailureClass.TRANSIENT,
                "Transient gRPC error (" + code + "): " + e.getMessage(), e);
        }
        throw new MutationApiException(FailureClass.PERMANENT,
            "Permanent gRPC error (" + code + "): " + e.getMessage(), e);
    }

    private <T> T parse(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new MutationApiException(FailureClass.PERMANENT,
                "Invalid payload JSON for " + type.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    // ── Payload records ───────────────────────────────────────────────────────

    private record CampaignPayload(String name, long budgetAmountMicros,
        String biddingStrategyType, String advertisingChannelType,
        String status, String startDate, String endDate) {}

    private record AdGroupPayload(String name, String status, long cpcBidMicros) {}

    private record AdHeadline(String text) {}

    private record AdDescription(String text) {}

    private record AdPayload(List<AdHeadline> headlines, List<AdDescription> descriptions,
        List<String> finalUrls) {}

    private record KeywordPayload(String text, String matchType,
        long cpcBidMicros, String status) {}
}
