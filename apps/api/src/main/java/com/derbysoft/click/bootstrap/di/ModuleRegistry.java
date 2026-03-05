package com.derbysoft.click.bootstrap.di;

import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.mapper.IntegrationInstanceMapper;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.repository.IntegrationInstanceJpaRepository;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.repository.IntegrationInstanceRepositoryImpl;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper.AccountBindingMapper;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper.AccountGraphStateMapper;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper.GoogleConnectionMapper;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountBindingJpaRepository;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountBindingRepositoryImpl;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountGraphStateJpaRepository;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountGraphStateRepositoryImpl;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.GoogleConnectionJpaRepository;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.GoogleConnectionRepositoryImpl;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.RawSnapshotMapper;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.SyncIncidentMapper;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.SyncJobMapper;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawSnapshotJpaRepository;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawSnapshotRepositoryImpl;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncIncidentJpaRepository;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncIncidentRepositoryImpl;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncJobJpaRepository;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncJobRepositoryImpl;
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

  /**
   * BC5 (google-ads-management): GoogleConnectionRepositoryImpl implements both
   * {@code GoogleConnectionRepository} (BC5 domain port) and
   * {@code GoogleAdsQueryPort} (BC5 public API port). Dual-interface pattern — same as
   * {@code IntegrationInstanceRepositoryImpl}.
   */
  @Bean
  public GoogleConnectionRepositoryImpl googleConnectionRepositoryImpl(
      GoogleConnectionJpaRepository jpaRepository,
      GoogleConnectionMapper mapper,
      AccountBindingJpaRepository bindingJpaRepository) {
    return new GoogleConnectionRepositoryImpl(jpaRepository, mapper, bindingJpaRepository);
  }

  @Bean
  public AccountBindingRepositoryImpl accountBindingRepositoryImpl(
      AccountBindingJpaRepository jpaRepository,
      AccountBindingMapper mapper) {
    return new AccountBindingRepositoryImpl(jpaRepository, mapper);
  }

  @Bean
  public AccountGraphStateRepositoryImpl accountGraphStateRepositoryImpl(
      AccountGraphStateJpaRepository jpaRepository,
      AccountGraphStateMapper mapper) {
    return new AccountGraphStateRepositoryImpl(jpaRepository, mapper);
  }

  // ── BC7 (ingestion) ──────────────────────────────────────────────────────

  @Bean
  public SyncJobRepositoryImpl syncJobRepositoryImpl(
      SyncJobJpaRepository jpaRepository,
      SyncJobMapper mapper) {
    return new SyncJobRepositoryImpl(jpaRepository, mapper);
  }

  @Bean
  public RawSnapshotRepositoryImpl rawSnapshotRepositoryImpl(
      RawSnapshotJpaRepository jpaRepository,
      RawSnapshotMapper mapper) {
    return new RawSnapshotRepositoryImpl(jpaRepository, mapper);
  }

  /**
   * BC7 (ingestion): SyncIncidentRepositoryImpl implements both
   * {@code SyncIncidentRepository} (BC7 domain port) and
   * {@code IngestionQueryPort} (BC7 public API port). Dual-interface pattern — same as
   * {@code GoogleConnectionRepositoryImpl} in googleadsmanagement.
   */
  @Bean
  public SyncIncidentRepositoryImpl syncIncidentRepositoryImpl(
      SyncIncidentJpaRepository incidentJpaRepository,
      SyncJobJpaRepository syncJobJpaRepository,
      SyncIncidentMapper incidentMapper,
      SyncJobMapper syncJobMapper) {
    return new SyncIncidentRepositoryImpl(incidentJpaRepository, syncJobJpaRepository,
        incidentMapper, syncJobMapper);
  }
}
