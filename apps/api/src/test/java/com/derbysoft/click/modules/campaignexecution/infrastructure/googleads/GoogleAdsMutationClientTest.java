package com.derbysoft.click.modules.campaignexecution.infrastructure.googleads;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.derbysoft.click.modules.campaignexecution.application.ports.GoogleAdsMutationPort.CampaignSpec;
import com.derbysoft.click.modules.campaignexecution.application.ports.GoogleAdsMutationPort.MutationResult;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.googleads.GoogleAdsConfig;
import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v23.services.CampaignBudgetServiceClient;
import com.google.ads.googleads.v23.services.CampaignServiceClient;
import com.google.ads.googleads.v23.services.GoogleAdsVersion;
import com.google.ads.googleads.v23.services.MutateCampaignBudgetResult;
import com.google.ads.googleads.v23.services.MutateCampaignBudgetsResponse;
import com.google.ads.googleads.v23.services.MutateCampaignResult;
import com.google.ads.googleads.v23.services.MutateCampaignsResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleAdsMutationClientTest {

    @Mock private GoogleAdsConfig config;
    @Mock private GoogleAdsClient googleAdsClient;

    private GoogleAdsMutationClient client;

    private static final String CUSTOMER_ID = "1234567890";
    private static final String MANAGER_ID = "9876543210";

    private static final String CAMPAIGN_PAYLOAD = """
        {
          "name": "Test Campaign",
          "budgetAmountMicros": 50000000,
          "biddingStrategyType": "MANUAL_CPC",
          "advertisingChannelType": "SEARCH",
          "status": "PAUSED",
          "startDate": null,
          "endDate": null
        }
        """;

    @BeforeEach
    void setUp() {
        client = new GoogleAdsMutationClient(config) {
            @Override
            protected GoogleAdsClient buildClient(String managerId) {
                return googleAdsClient;
            }
        };
    }

    @Test
    void shouldCreateCampaignAndReturnResourceName() {
        GoogleAdsVersion latestVersion = mock(GoogleAdsVersion.class);
        CampaignBudgetServiceClient budgetServiceClient = mock(CampaignBudgetServiceClient.class);
        CampaignServiceClient campaignServiceClient = mock(CampaignServiceClient.class);

        when(googleAdsClient.getLatestVersion()).thenReturn(latestVersion);
        when(latestVersion.createCampaignBudgetServiceClient()).thenReturn(budgetServiceClient);
        when(budgetServiceClient.mutateCampaignBudgets(anyString(), anyList()))
            .thenReturn(MutateCampaignBudgetsResponse.newBuilder()
                .addResults(MutateCampaignBudgetResult.newBuilder()
                    .setResourceName("customers/1234567890/campaignBudgets/111")
                    .build())
                .build());
        when(latestVersion.createCampaignServiceClient()).thenReturn(campaignServiceClient);
        when(campaignServiceClient.mutateCampaigns(anyString(), anyList()))
            .thenReturn(MutateCampaignsResponse.newBuilder()
                .addResults(MutateCampaignResult.newBuilder()
                    .setResourceName("customers/1234567890/campaigns/999")
                    .build())
                .build());

        MutationResult result = client.createCampaign(CUSTOMER_ID, MANAGER_ID,
            new CampaignSpec(null, CAMPAIGN_PAYLOAD));

        assertThat(result.success()).isTrue();
        assertThat(result.resourceId()).isEqualTo("customers/1234567890/campaigns/999");
    }

    @Test
    void shouldClassifyAuthFailureAsMutationAuthException() {
        // UNAUTHENTICATED gRPC status → PERMANENT MutationApiException
        GoogleAdsVersion latestVersion = mock(GoogleAdsVersion.class);
        CampaignBudgetServiceClient budgetServiceClient = mock(CampaignBudgetServiceClient.class);

        when(googleAdsClient.getLatestVersion()).thenReturn(latestVersion);
        when(latestVersion.createCampaignBudgetServiceClient()).thenReturn(budgetServiceClient);
        when(budgetServiceClient.mutateCampaignBudgets(anyString(), anyList()))
            .thenThrow(new StatusRuntimeException(
                Status.UNAUTHENTICATED.withDescription("invalid token")));

        assertThatThrownBy(() -> client.createCampaign(CUSTOMER_ID, MANAGER_ID,
                new CampaignSpec(null, CAMPAIGN_PAYLOAD)))
            .isInstanceOf(MutationApiException.class)
            .satisfies(e -> assertThat(((MutationApiException) e).getFailureClass())
                .isEqualTo(FailureClass.PERMANENT));
    }

    @Test
    void shouldClassifyTransientGrpcErrorAsMutationApiExceptionTransient() {
        GoogleAdsVersion latestVersion = mock(GoogleAdsVersion.class);
        CampaignBudgetServiceClient budgetServiceClient = mock(CampaignBudgetServiceClient.class);

        when(googleAdsClient.getLatestVersion()).thenReturn(latestVersion);
        when(latestVersion.createCampaignBudgetServiceClient()).thenReturn(budgetServiceClient);
        when(budgetServiceClient.mutateCampaignBudgets(anyString(), anyList()))
            .thenThrow(new StatusRuntimeException(
                Status.DEADLINE_EXCEEDED.withDescription("timeout")));

        assertThatThrownBy(() -> client.createCampaign(CUSTOMER_ID, MANAGER_ID,
                new CampaignSpec(null, CAMPAIGN_PAYLOAD)))
            .isInstanceOf(MutationApiException.class)
            .satisfies(e -> assertThat(((MutationApiException) e).getFailureClass())
                .isEqualTo(FailureClass.TRANSIENT));
    }

    @Test
    void shouldClassifyPermanentGrpcErrorAsMutationApiExceptionPermanent() {
        GoogleAdsVersion latestVersion = mock(GoogleAdsVersion.class);
        CampaignBudgetServiceClient budgetServiceClient = mock(CampaignBudgetServiceClient.class);

        when(googleAdsClient.getLatestVersion()).thenReturn(latestVersion);
        when(latestVersion.createCampaignBudgetServiceClient()).thenReturn(budgetServiceClient);
        when(budgetServiceClient.mutateCampaignBudgets(anyString(), anyList()))
            .thenThrow(new StatusRuntimeException(
                Status.INVALID_ARGUMENT.withDescription("bad field")));

        assertThatThrownBy(() -> client.createCampaign(CUSTOMER_ID, MANAGER_ID,
                new CampaignSpec(null, CAMPAIGN_PAYLOAD)))
            .isInstanceOf(MutationApiException.class)
            .satisfies(e -> assertThat(((MutationApiException) e).getFailureClass())
                .isEqualTo(FailureClass.PERMANENT));
    }
}
