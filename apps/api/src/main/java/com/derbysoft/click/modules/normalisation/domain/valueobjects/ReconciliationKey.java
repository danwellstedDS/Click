package com.derbysoft.click.modules.normalisation.domain.valueobjects;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;

public record ReconciliationKey(String value) {

    public static ReconciliationKey from(UUID integrationId, String accountId, String campaignId, LocalDate reportDate) {
        String raw = integrationId + ":" + accountId + ":" + campaignId + ":" + reportDate;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return new ReconciliationKey(HexFormat.of().formatHex(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
