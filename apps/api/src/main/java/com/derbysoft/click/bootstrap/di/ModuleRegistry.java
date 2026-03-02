package com.derbysoft.click.bootstrap.di;

import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.mapper.IntegrationInstanceMapper;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.repository.IntegrationInstanceJpaRepository;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.repository.IntegrationInstanceRepositoryImpl;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository.PropertyGroupJpaRepository;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository.PropertyGroupRepositoryImpl;
import com.derbysoft.click.modules.tenantgovernance.api.ports.TenantGovernancePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires cross-BC dependencies. Each {@code @Bean} here represents a deliberate
 * dependency from one bounded context to another's public API port.
 *
 * <p>BC1 (identity-access) → BC3 (organisation-structure): PropertyGroupQueryPort (findInfoById only,
 * used by AuthCommandHandler for /me and /switch-tenant; no longer used by UserManagementHandler)
 *
 * <p>BC4 (channel-integration) → BC2 (tenant-governance): TenantGovernancePort (stub)
 *
 * <p>BC4 (channel-integration): IntegrationInstanceRepositoryImpl implements both
 * {@code IntegrationInstanceRepository} (BC4 domain port) and
 * {@code IntegrationQueryPort} (BC4 public API port).
 *
 * <p>{@code PropertyGroupRepositoryImpl} implements both {@code PropertyGroupRepository}
 * (BC3 domain port) and {@code PropertyGroupQueryPort} (BC3 public API port). Declaring
 * it here rather than via {@code @Repository} component scan makes the cross-BC wiring
 * explicit. Spring auto-wires it wherever either interface is required.
 */
@Configuration
public class ModuleRegistry {

  @Bean
  public PropertyGroupRepositoryImpl propertyGroupRepositoryImpl(
      PropertyGroupJpaRepository jpaRepository) {
    return new PropertyGroupRepositoryImpl(jpaRepository);
  }

  /**
   * Stub: always permits integration creation. Replace with a real implementation
   * when BC2's application layer is built.
   */
  @Bean
  public TenantGovernancePort tenantGovernancePort() {
    return tenantId -> { /* always permit */ };
  }

  @Bean
  public IntegrationInstanceRepositoryImpl integrationInstanceRepositoryImpl(
      IntegrationInstanceJpaRepository jpaRepository,
      IntegrationInstanceMapper mapper) {
    return new IntegrationInstanceRepositoryImpl(jpaRepository, mapper);
  }
}
