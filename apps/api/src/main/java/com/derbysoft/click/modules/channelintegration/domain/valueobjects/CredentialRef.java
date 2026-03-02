package com.derbysoft.click.modules.channelintegration.domain.valueobjects;

import java.util.UUID;

public record CredentialRef(UUID credentialId) {
    public CredentialRef {
        if (credentialId == null) {
            throw new IllegalArgumentException("credentialId must not be null");
        }
    }
}
