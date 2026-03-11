package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingOverrideJpaRepository extends JpaRepository<MappingOverrideEntity, UUID> {

    List<MappingOverrideEntity> findByTenantIdAndStatus(UUID tenantId, String status);

    Optional<MappingOverrideEntity> findByTenantIdAndCustomerAccountIdAndStatusAndCampaignId(
        UUID tenantId, String customerAccountId, String status, String campaignId);

    Optional<MappingOverrideEntity> findByTenantIdAndCustomerAccountIdAndStatusAndCampaignIdIsNull(
        UUID tenantId, String customerAccountId, String status);
}
