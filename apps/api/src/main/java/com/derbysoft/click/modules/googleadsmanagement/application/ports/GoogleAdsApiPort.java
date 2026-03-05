package com.derbysoft.click.modules.googleadsmanagement.application.ports;

import java.util.List;

public interface GoogleAdsApiPort {
    List<DiscoveredAccount> listAccessibleAccounts(String managerId, String credentialPath);
    boolean validateCredential(String managerId, String credentialPath);

    record DiscoveredAccount(String customerId, String name, String currencyCode, String timeZone) {}
}
