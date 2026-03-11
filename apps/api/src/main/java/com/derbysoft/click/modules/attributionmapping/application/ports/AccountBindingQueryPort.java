package com.derbysoft.click.modules.attributionmapping.application.ports;

import java.util.List;
import java.util.UUID;

public interface AccountBindingQueryPort {

    List<ActiveBindingData> findActiveByTenantId(UUID tenantId);

    record ActiveBindingData(
        UUID id,
        String customerId,
        UUID orgNodeId,
        String orgScopeType
    ) {}
}
