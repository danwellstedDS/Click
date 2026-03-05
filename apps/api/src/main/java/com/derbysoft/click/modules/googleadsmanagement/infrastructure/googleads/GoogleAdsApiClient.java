package com.derbysoft.click.modules.googleadsmanagement.infrastructure.googleads;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v23.errors.GoogleAdsException;
import com.google.ads.googleads.v23.services.CustomerServiceClient;
import com.google.ads.googleads.v23.services.ListAccessibleCustomersRequest;
import com.google.ads.googleads.v23.services.ListAccessibleCustomersResponse;
import com.google.auth.oauth2.GoogleCredentials;
import com.derbysoft.click.modules.googleadsmanagement.application.ports.GoogleAdsApiPort;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GoogleAdsApiClient implements GoogleAdsApiPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleAdsApiClient.class);
    private static final String ADS_SCOPE = "https://www.googleapis.com/auth/adwords";

    private final GoogleAdsConfig config;

    public GoogleAdsApiClient(GoogleAdsConfig config) {
        this.config = config;
    }

    @Override
    public List<DiscoveredAccount> listAccessibleAccounts(String managerId, String credentialPath) {
        try {
            GoogleAdsClient googleAdsClient = buildClient(managerId, credentialPath);
            try (CustomerServiceClient customerServiceClient =
                    googleAdsClient.getLatestVersion().createCustomerServiceClient()) {
                ListAccessibleCustomersResponse response =
                    customerServiceClient.listAccessibleCustomers(
                        ListAccessibleCustomersRequest.newBuilder().build());
                return response.getResourceNamesList().stream()
                    .map(resourceName -> {
                        String customerId = extractCustomerId(resourceName);
                        return new DiscoveredAccount(customerId, null, null, null);
                    })
                    .toList();
            }
        } catch (GoogleAdsException e) {
            log.error("Google Ads API error listing accounts for manager {}: {}", managerId, e.getMessage());
            throw new RuntimeException("Google Ads API error: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to load credentials from {}: {}", credentialPath, e.getMessage());
            throw new RuntimeException("Failed to load Google Ads credentials: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateCredential(String managerId, String credentialPath) {
        try {
            listAccessibleAccounts(managerId, credentialPath);
            return true;
        } catch (Exception e) {
            log.warn("Credential validation failed for manager {}: {}", managerId, e.getMessage());
            return false;
        }
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

    private static String extractCustomerId(String resourceName) {
        // resource name format: "customers/1234567890"
        int slash = resourceName.lastIndexOf('/');
        return slash >= 0 ? resourceName.substring(slash + 1) : resourceName;
    }
}
