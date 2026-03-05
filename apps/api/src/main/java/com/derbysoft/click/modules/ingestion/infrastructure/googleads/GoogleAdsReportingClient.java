package com.derbysoft.click.modules.ingestion.infrastructure.googleads;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v23.errors.GoogleAdsError;
import com.google.ads.googleads.v23.errors.GoogleAdsException;
import com.google.ads.googleads.v23.services.GoogleAdsRow;
import com.google.ads.googleads.v23.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v23.services.SearchGoogleAdsStreamRequest;
import com.google.ads.googleads.v23.services.SearchGoogleAdsStreamResponse;
import com.google.auth.oauth2.GoogleCredentials;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.googleads.GoogleAdsConfig;
import com.derbysoft.click.modules.ingestion.application.ports.GoogleAdsReportingPort;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GoogleAdsReportingClient implements GoogleAdsReportingPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleAdsReportingClient.class);
    private static final String ADS_SCOPE = "https://www.googleapis.com/auth/adwords";

    private final GoogleAdsConfig config;

    public GoogleAdsReportingClient(GoogleAdsConfig config) {
        this.config = config;
    }

    @Override
    public List<CampaignRow> fetchCampaignMetrics(String customerId, String managerId,
                                                   String credentialPath, DateWindow window) {
        try {
            GoogleAdsClient googleAdsClient = buildClient(managerId, credentialPath);
            String gaql = buildGaql(window);

            try (GoogleAdsServiceClient serviceClient =
                     googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {

                SearchGoogleAdsStreamRequest request = SearchGoogleAdsStreamRequest.newBuilder()
                    .setCustomerId(customerId.replace("-", ""))
                    .setQuery(gaql)
                    .build();

                List<CampaignRow> results = new ArrayList<>();
                Iterable<SearchGoogleAdsStreamResponse> stream =
                    serviceClient.searchStreamCallable().call(request);

                for (SearchGoogleAdsStreamResponse response : stream) {
                    for (GoogleAdsRow row : response.getResultsList()) {
                        results.add(mapRow(row));
                    }
                }

                return results;
            }

        } catch (GoogleAdsException e) {
            handleGoogleAdsException(e);
            throw new IngestionFetchException(FailureClass.TRANSIENT, "Unexpected error", e);
        } catch (StatusRuntimeException e) {
            handleStatusRuntimeException(e);
            throw new IngestionFetchException(FailureClass.TRANSIENT, "gRPC error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IngestionFetchException(FailureClass.TRANSIENT,
                "Failed to load credentials: " + e.getMessage(), e);
        }
    }

    private String buildGaql(DateWindow window) {
        return String.format(
            "SELECT campaign.id, campaign.name, " +
            "metrics.clicks, metrics.impressions, metrics.cost_micros, metrics.conversions, " +
            "segments.date " +
            "FROM campaign " +
            "WHERE segments.date BETWEEN '%s' AND '%s'",
            window.from(), window.to()
        );
    }

    private CampaignRow mapRow(GoogleAdsRow row) {
        String campaignId = String.valueOf(row.getCampaign().getId());
        String campaignName = row.getCampaign().getName();
        long clicks = row.getMetrics().getClicks();
        long impressions = row.getMetrics().getImpressions();
        long costMicros = row.getMetrics().getCostMicros();
        double conversions = row.getMetrics().getConversions();
        LocalDate reportDate = LocalDate.parse(row.getSegments().getDate());
        return new CampaignRow(campaignId, campaignName, clicks, impressions, costMicros, conversions, reportDate);
    }

    private void handleGoogleAdsException(GoogleAdsException e) {
        for (GoogleAdsError error : e.getGoogleAdsFailure().getErrorsList()) {
            String errorCode = error.getErrorCode().toString();
            if (errorCode.contains("OAUTH_TOKEN") ||
                errorCode.contains("NOT_AUTHORIZED") ||
                errorCode.contains("CUSTOMER_NOT_FOUND")) {
                throw new IngestionAuthException("Google Ads auth failure: " + errorCode, e);
            }
        }
        throw new IngestionFetchException(FailureClass.PERMANENT,
            "Google Ads API error: " + e.getMessage(), e);
    }

    private void handleStatusRuntimeException(StatusRuntimeException e) {
        Status.Code code = e.getStatus().getCode();
        if (code == Status.Code.DEADLINE_EXCEEDED ||
            code == Status.Code.RESOURCE_EXHAUSTED ||
            code == Status.Code.UNAVAILABLE) {
            throw new IngestionFetchException(FailureClass.TRANSIENT,
                "Transient gRPC error (" + code + "): " + e.getMessage(), e);
        }
        throw new IngestionFetchException(FailureClass.PERMANENT,
            "Permanent gRPC error (" + code + "): " + e.getMessage(), e);
    }

    private GoogleAdsClient buildClient(String managerId, String credentialPath) throws IOException {
        String path = credentialPath != null ? credentialPath : config.getCredentialsPath();
        GoogleCredentials credentials = GoogleCredentials
            .fromStream(new FileInputStream(path))
            .createScoped(Collections.singletonList(ADS_SCOPE));

        long parsedManagerId = Long.parseLong(managerId.replace("-", ""));

        return GoogleAdsClient.newBuilder()
            .setCredentials(credentials)
            .setDeveloperToken(config.getDeveloperToken())
            .setLoginCustomerId(parsedManagerId)
            .build();
    }
}
