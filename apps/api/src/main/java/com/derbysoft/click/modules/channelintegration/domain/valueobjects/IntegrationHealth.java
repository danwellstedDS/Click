package com.derbysoft.click.modules.channelintegration.domain.valueobjects;

import java.time.Instant;

public record IntegrationHealth(Instant checkedAt, String errorMessage) {}
