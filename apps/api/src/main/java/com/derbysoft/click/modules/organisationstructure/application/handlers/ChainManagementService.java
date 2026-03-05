package com.derbysoft.click.modules.organisationstructure.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.identityaccess.domain.TenantMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.organisationstructure.domain.PropertyGroupRepository;
import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.domain.events.ChainCreated;
import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import com.derbysoft.click.modules.tenantgovernance.domain.AccessScopeRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.ScopeAccessGrantRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.entities.AccessScope;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.GrantRole;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ScopeType;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChainManagementService {

  private final PropertyGroupRepository repository;
  private final InProcessEventBus eventBus;
  private final TenantMembershipRepository tenantMembershipRepository;
  private final AccessScopeRepository accessScopeRepository;
  private final ScopeAccessGrantRepository scopeAccessGrantRepository;

  public ChainManagementService(
      PropertyGroupRepository repository,
      InProcessEventBus eventBus,
      TenantMembershipRepository tenantMembershipRepository,
      AccessScopeRepository accessScopeRepository,
      ScopeAccessGrantRepository scopeAccessGrantRepository
  ) {
    this.repository = repository;
    this.eventBus = eventBus;
    this.tenantMembershipRepository = tenantMembershipRepository;
    this.accessScopeRepository = accessScopeRepository;
    this.scopeAccessGrantRepository = scopeAccessGrantRepository;
  }

  public List<PropertyGroup> listChains(UserPrincipal principal) {
    requireAdmin(principal);
    return repository.findAll();
  }

  @Transactional
  public PropertyGroup createChain(
      String name, String timezone, String currency,
      UUID organizationId,
      UserPrincipal principal
  ) {
    requireAdmin(principal);

    if (name == null || name.isBlank()) {
      throw new DomainError.ValidationError("CHAIN_400", "name is required");
    }

    Instant now = Instant.now();
    // Pass null UUID — @UuidGenerator assigns it during persist(); use saved.getId() for the event.
    PropertyGroup saved = repository.save(
        PropertyGroup.create(null, null, name, timezone, currency, organizationId, now, now));

    tenantMembershipRepository.create(UUID.randomUUID(), principal.userId(), saved.getId(), Role.ADMIN);

    if (organizationId != null) {
      AccessScope scope = accessScopeRepository.create(ScopeType.PROPERTY_GROUP, saved.getId(), null, null);
      scopeAccessGrantRepository.create(organizationId, scope.id(), GrantRole.ADMIN);
    }

    eventBus.publish(EventEnvelope.of(
        ChainCreated.class.getSimpleName(),
        new ChainCreated(saved.getId(), name, now)));

    return saved;
  }

  @Transactional
  public PropertyGroup updateStatus(UUID id, ChainStatus newStatus, UserPrincipal principal) {
    requireAdmin(principal);

    PropertyGroup chain = repository.findById(id)
        .orElseThrow(() -> new DomainError.NotFound("CHAIN_404", "Chain not found: " + id));

    if (newStatus == ChainStatus.ACTIVE) {
      chain.activate();
    } else {
      chain.deactivate();
    }

    return repository.save(chain);
  }

  public PropertyGroup findById(UUID id, UserPrincipal principal) {
    requireAdmin(principal);
    return repository.findById(id)
        .orElseThrow(() -> new DomainError.NotFound("CHAIN_404", "Chain not found: " + id));
  }

  private static void requireAdmin(UserPrincipal principal) {
    if (principal.role() != Role.ADMIN) {
      throw new DomainError.Forbidden("AUTH_403", "Admin access required");
    }
  }
}
