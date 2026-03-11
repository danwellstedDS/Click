package com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.request;

import java.util.UUID;

public record SetMappingOverrideRequest(
    String customerAccountId,
    String campaignId,          // null for ACCOUNT scope
    UUID targetOrgNodeId,
    String targetScopeType,
    String reason
) {}
