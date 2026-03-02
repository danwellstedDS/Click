package com.derbysoft.click.modules.channelintegration.interfaces.http.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttachCredentialRequest(
    @NotNull UUID credentialId
) {}
