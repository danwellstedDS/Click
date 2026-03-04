package com.derbysoft.click.modules.organisationstructure.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.identityaccess.domain.TenantMembershipRepository;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.modules.organisationstructure.domain.PropertyGroupRepository;
import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import com.derbysoft.click.modules.tenantgovernance.domain.AccessScopeRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.ScopeAccessGrantRepository;
import com.derbysoft.click.modules.tenantgovernance.domain.entities.AccessScope;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.GrantRole;
import com.derbysoft.click.modules.tenantgovernance.domain.valueobjects.ScopeType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChainManagementServiceTest {

  @Mock
  private PropertyGroupRepository repository;

  @Mock
  private InProcessEventBus eventBus;

  @Mock
  private TenantMembershipRepository tenantMembershipRepository;

  @Mock
  private AccessScopeRepository accessScopeRepository;

  @Mock
  private ScopeAccessGrantRepository scopeAccessGrantRepository;

  @InjectMocks
  private ChainManagementService chainManagementService;

  private static final UUID CHAIN_ID = UUID.randomUUID();

  private static UserPrincipal adminPrincipal() {
    return new UserPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@example.com", Role.ADMIN);
  }

  private static UserPrincipal viewerPrincipal() {
    return new UserPrincipal(UUID.randomUUID(), UUID.randomUUID(), "viewer@example.com", Role.VIEWER);
  }

  private static PropertyGroup activeChain(UUID id) {
    return PropertyGroup.reconstitute(id, null, "Test Chain", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.ACTIVE);
  }

  private static PropertyGroup inactiveChain(UUID id) {
    return PropertyGroup.reconstitute(id, null, "Test Chain", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.INACTIVE);
  }

  @Test
  void shouldListAllChains() {
    when(repository.findAll()).thenReturn(List.of(activeChain(CHAIN_ID)));

    List<PropertyGroup> result = chainManagementService.listChains(adminPrincipal());

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(CHAIN_ID);
  }

  @Test
  void shouldThrowForbiddenWhenNonAdmin() {
    assertThatThrownBy(() -> chainManagementService.listChains(viewerPrincipal()))
        .isInstanceOf(DomainError.Forbidden.class);
  }

  @Test
  void shouldCreateChainWithActiveStatus() {
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    PropertyGroup result = chainManagementService.createChain("New Chain", "UTC", "USD", null, adminPrincipal());

    assertThat(result.getName()).isEqualTo("New Chain");
    assertThat(result.getStatus()).isEqualTo(ChainStatus.ACTIVE);
    verify(eventBus).publish(any());
  }

  @Test
  void shouldThrowValidationErrorWhenNameBlank() {
    assertThatThrownBy(() -> chainManagementService.createChain("", "UTC", "USD", null, adminPrincipal()))
        .isInstanceOf(DomainError.ValidationError.class);
  }

  @Test
  void shouldCreateTenantMembershipForCreatorOnChainCreation() {
    UserPrincipal admin = adminPrincipal();
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    chainManagementService.createChain("New Chain", "UTC", "USD", null, admin);

    verify(tenantMembershipRepository).create(any(UUID.class), eq(admin.userId()), any(UUID.class), eq(Role.ADMIN));
  }

  @Test
  void shouldCreateAccessScopeAndGrantWhenOrgIdProvided() {
    UUID orgId = UUID.randomUUID();
    UUID scopeId = UUID.randomUUID();
    PropertyGroup saved = activeChain(CHAIN_ID);
    when(repository.save(any())).thenReturn(saved);
    AccessScope scope = new AccessScope(scopeId, ScopeType.PROPERTY_GROUP, CHAIN_ID, null, null, Instant.now());
    when(accessScopeRepository.create(eq(ScopeType.PROPERTY_GROUP), eq(CHAIN_ID), isNull(), isNull())).thenReturn(scope);

    chainManagementService.createChain("New Chain", "UTC", "USD", orgId, adminPrincipal());

    verify(accessScopeRepository).create(ScopeType.PROPERTY_GROUP, CHAIN_ID, null, null);
    verify(scopeAccessGrantRepository).create(orgId, scopeId, GrantRole.ADMIN);
  }

  @Test
  void shouldNotCreateScopeGrantWhenOrgIdNull() {
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    chainManagementService.createChain("New Chain", "UTC", "USD", null, adminPrincipal());

    verify(accessScopeRepository, never()).create(any(), any(), any(), any());
    verify(scopeAccessGrantRepository, never()).create(any(), any(), any());
  }

  @Test
  void shouldActivateInactiveChain() {
    PropertyGroup chain = inactiveChain(CHAIN_ID);
    when(repository.findById(CHAIN_ID)).thenReturn(Optional.of(chain));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    PropertyGroup result = chainManagementService.updateStatus(CHAIN_ID, ChainStatus.ACTIVE, adminPrincipal());

    assertThat(result.getStatus()).isEqualTo(ChainStatus.ACTIVE);
  }

  @Test
  void shouldDeactivateActiveChain() {
    PropertyGroup chain = activeChain(CHAIN_ID);
    when(repository.findById(CHAIN_ID)).thenReturn(Optional.of(chain));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    PropertyGroup result = chainManagementService.updateStatus(CHAIN_ID, ChainStatus.INACTIVE, adminPrincipal());

    assertThat(result.getStatus()).isEqualTo(ChainStatus.INACTIVE);
  }

  @Test
  void shouldThrowConflictWhenActivatingAlreadyActive() {
    PropertyGroup chain = activeChain(CHAIN_ID);
    when(repository.findById(CHAIN_ID)).thenReturn(Optional.of(chain));

    assertThatThrownBy(() -> chainManagementService.updateStatus(CHAIN_ID, ChainStatus.ACTIVE, adminPrincipal()))
        .isInstanceOf(DomainError.Conflict.class);
  }

  @Test
  void shouldThrowNotFoundWhenChainMissing() {
    when(repository.findById(CHAIN_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> chainManagementService.findById(CHAIN_ID, adminPrincipal()))
        .isInstanceOf(DomainError.NotFound.class);
  }
}
