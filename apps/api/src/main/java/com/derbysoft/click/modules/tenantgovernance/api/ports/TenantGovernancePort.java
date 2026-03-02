package com.derbysoft.click.modules.tenantgovernance.api.ports;

import java.util.UUID;

/**
 * Public API port for BC2 (TenantGovernance).
 * BC4 (ChannelIntegration) calls this before creating an IntegrationInstance.
 * The stub implementation in ModuleRegistry always permits; replace with a real
 * implementation when BC2's application layer is built.
 */
public interface TenantGovernancePort {
    /**
     * Asserts that the tenant is permitted to create a new channel integration.
     *
     * @throws com.derbysoft.click.sharedkernel.domain.errors.DomainError.Forbidden if the tenant
     *         is not permitted to create integrations (e.g., plan limit exceeded, account suspended)
     */
    void assertCanCreateIntegration(UUID tenantId);
}
