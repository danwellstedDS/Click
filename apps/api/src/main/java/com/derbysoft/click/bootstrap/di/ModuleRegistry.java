package com.derbysoft.click.bootstrap.di;

import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.mapper.MappingRunMapper;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository.MappedFactJpaRepository;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository.MappingRunJpaRepository;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository.MappingRunRepositoryImpl;
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
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.CampaignPlanMapper;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.DriftReportMapper;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.ExecutionIncidentMapper;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.PlanItemMapper;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.PlanRevisionMapper;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.WriteActionMapper;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.CampaignPlanJpaRepository;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.CampaignPlanRepositoryImpl;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.DriftReportJpaRepository;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.DriftReportRepositoryImpl;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.ExecutionIncidentJpaRepository;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.ExecutionIncidentRepositoryImpl;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.PlanItemJpaRepository;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.PlanItemRepositoryImpl;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.PlanRevisionJpaRepository;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.PlanRevisionRepositoryImpl;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.WriteActionJpaRepository;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository.WriteActionRepositoryImpl;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.RawSnapshotMapper;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.SyncIncidentMapper;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.SyncJobMapper;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawCampaignRowJpaRepository;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawCampaignRowQueryAdapter;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawSnapshotJpaRepository;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawSnapshotRepositoryImpl;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncIncidentJpaRepository;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncIncidentRepositoryImpl;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncJobJpaRepository;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncJobRepositoryImpl;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountBindingQueryAdapter;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository.CanonicalFactQueryAdapter;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.mapper.CanonicalBatchMapper;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository.CanonicalBatchJpaRepository;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository.CanonicalBatchRepositoryImpl;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository.CanonicalFactJpaRepository;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository.PropertyGroupJpaRepository;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository.PropertyGroupRepositoryImpl;
import com.derbysoft.click.modules.campaignexecution.application.ports.SnapshotQueryPort;
import com.derbysoft.click.modules.tenantgovernance.api.ports.TenantGovernancePort;
import java.util.Optional;
import java.util.UUID;
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
   * Stub: always permits all governance checks. Replace with a real implementation
   * when BC2's application layer is built.
   */
  @Bean
  public TenantGovernancePort tenantGovernancePort() {
    return new TenantGovernancePort() {
      @Override
      public void assertCanCreateIntegration(UUID tenantId) { /* always permit */ }

      @Override
      public void assertCanExecuteCampaigns(UUID tenantId) { /* always permit */ }
    };
  }

  /**
   * Stub: returns empty snapshots. Replace with a real BC7 adapter when
   * per-resource snapshot lookup is available in the ingestion module.
   */
  @Bean
  public SnapshotQueryPort snapshotQueryPort() {
    return resourceId -> Optional.empty();
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
  // ── BC6 (campaign-execution) ─────────────────────────────────────────────

  @Bean
  public CampaignPlanRepositoryImpl campaignPlanRepositoryImpl(
      CampaignPlanJpaRepository jpaRepository,
      CampaignPlanMapper mapper) {
    return new CampaignPlanRepositoryImpl(jpaRepository, mapper);
  }

  @Bean
  public PlanRevisionRepositoryImpl planRevisionRepositoryImpl(
      PlanRevisionJpaRepository jpaRepository,
      PlanRevisionMapper mapper) {
    return new PlanRevisionRepositoryImpl(jpaRepository, mapper);
  }

  @Bean
  public PlanItemRepositoryImpl planItemRepositoryImpl(
      PlanItemJpaRepository jpaRepository,
      PlanItemMapper mapper) {
    return new PlanItemRepositoryImpl(jpaRepository, mapper);
  }

  /**
   * BC6 (campaign-execution): WriteActionRepositoryImpl implements both
   * {@code WriteActionRepository} (BC6 domain port) and
   * {@code CampaignManagementQueryPort} (BC6 public API port). Dual-interface pattern — same as
   * {@code SyncIncidentRepositoryImpl} in ingestion.
   */
  @Bean
  public WriteActionRepositoryImpl writeActionRepositoryImpl(
      WriteActionJpaRepository writeActionJpaRepository,
      PlanRevisionJpaRepository planRevisionJpaRepository,
      PlanItemJpaRepository planItemJpaRepository,
      ExecutionIncidentJpaRepository executionIncidentJpaRepository,
      WriteActionMapper writeActionMapper) {
    return new WriteActionRepositoryImpl(writeActionJpaRepository, planRevisionJpaRepository,
        planItemJpaRepository, executionIncidentJpaRepository, writeActionMapper);
  }

  @Bean
  public ExecutionIncidentRepositoryImpl executionIncidentRepositoryImpl(
      ExecutionIncidentJpaRepository jpaRepository,
      ExecutionIncidentMapper mapper) {
    return new ExecutionIncidentRepositoryImpl(jpaRepository, mapper);
  }

  @Bean
  public DriftReportRepositoryImpl driftReportRepositoryImpl(
      DriftReportJpaRepository jpaRepository,
      DriftReportMapper mapper) {
    return new DriftReportRepositoryImpl(jpaRepository, mapper);
  }

  @Bean
  public SyncIncidentRepositoryImpl syncIncidentRepositoryImpl(
      SyncIncidentJpaRepository incidentJpaRepository,
      SyncJobJpaRepository syncJobJpaRepository,
      SyncIncidentMapper incidentMapper,
      SyncJobMapper syncJobMapper) {
    return new SyncIncidentRepositoryImpl(incidentJpaRepository, syncJobJpaRepository,
        incidentMapper, syncJobMapper);
  }

  // ── BC8 (normalisation) ───────────────────────────────────────────────────

  /**
   * BC8 (normalisation): CanonicalBatchRepositoryImpl implements both
   * {@code CanonicalBatchRepository} (BC8 domain port) and
   * {@code NormalisationQueryPort} (BC8 public API port). Dual-interface pattern.
   */
  @Bean
  public CanonicalBatchRepositoryImpl canonicalBatchRepositoryImpl(
      CanonicalBatchJpaRepository batchJpaRepository,
      CanonicalFactJpaRepository factJpaRepository,
      CanonicalBatchMapper mapper) {
    return new CanonicalBatchRepositoryImpl(batchJpaRepository, factJpaRepository, mapper);
  }

  /**
   * BC7 → BC8 adapter: exposes BC7's raw campaign rows to BC8's normalisation pipeline
   * via {@code RawCampaignRowQueryPort}.
   */
  @Bean
  public RawCampaignRowQueryAdapter rawCampaignRowQueryAdapter(
      RawCampaignRowJpaRepository rawCampaignRowJpaRepository) {
    return new RawCampaignRowQueryAdapter(rawCampaignRowJpaRepository);
  }

  // ── BC9 (attribution-mapping) ─────────────────────────────────────────────

  /**
   * BC9 (attribution-mapping): MappingRunRepositoryImpl implements both
   * {@code MappingRunRepository} (BC9 domain port) and
   * {@code AttributionQueryPort} (BC9 public API port). Dual-interface pattern.
   */
  @Bean
  public MappingRunRepositoryImpl mappingRunRepositoryImpl(
      MappingRunJpaRepository runJpaRepository,
      MappedFactJpaRepository factJpaRepository,
      MappingRunMapper runMapper) {
    return new MappingRunRepositoryImpl(runJpaRepository, factJpaRepository, runMapper);
  }

  /**
   * BC8 → BC9 adapter: exposes BC8's canonical facts to BC9's attribution pipeline
   * via {@code CanonicalFactQueryPort}.
   */
  @Bean
  public CanonicalFactQueryAdapter canonicalFactQueryAdapter(
      CanonicalFactJpaRepository canonicalFactJpaRepository) {
    return new CanonicalFactQueryAdapter(canonicalFactJpaRepository);
  }

  /**
   * BC5 → BC9 adapter: exposes BC5's account bindings to BC9's attribution pipeline
   * via {@code AccountBindingQueryPort}.
   */
  @Bean
  public AccountBindingQueryAdapter accountBindingQueryAdapter(
      AccountBindingJpaRepository accountBindingJpaRepository) {
    return new AccountBindingQueryAdapter(accountBindingJpaRepository);
  }
}
