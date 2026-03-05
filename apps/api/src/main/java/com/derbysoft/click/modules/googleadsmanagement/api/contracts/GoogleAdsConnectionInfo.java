package com.derbysoft.click.modules.googleadsmanagement.api.contracts;

import java.util.UUID;

public record GoogleAdsConnectionInfo(UUID id, UUID tenantId, String managerId, String status) {}
