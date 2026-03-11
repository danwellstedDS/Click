package com.derbysoft.click.modules.attributionmapping.application.services;

import com.derbysoft.click.modules.attributionmapping.domain.MappingOverrideRepository;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OverrideResolver {

    private final MappingOverrideRepository overrideRepository;

    public OverrideResolver(MappingOverrideRepository overrideRepository) {
        this.overrideRepository = overrideRepository;
    }

    /**
     * Finds the most specific active override for a fact.
     * Precedence: ACCOUNT_CAMPAIGN (exact match on campaignId) > ACCOUNT (no campaignId).
     */
    public Optional<MappingOverrideEntity> resolve(
        UUID tenantId, String customerAccountId, String campaignId
    ) {
        return overrideRepository.findMatchingOverride(tenantId, customerAccountId, campaignId);
    }
}
