package com.derbysoft.click.modules.identityaccess.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantMembershipTest {

  @Test
  void shouldHoldTenantIdDirectly() {
    UUID membershipId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID propertyGroupId = UUID.randomUUID(); // This is PropertyGroup.id — the tenant

    TenantMembership membership = new TenantMembership(membershipId, userId, propertyGroupId, Role.ADMIN, Instant.now());

    // tenantId must be PropertyGroup.id directly, not Organization.id
    assertThat(membership.tenantId()).isEqualTo(propertyGroupId);
    assertThat(membership.userId()).isEqualTo(userId);
    assertThat(membership.id()).isEqualTo(membershipId);
  }

  @Test
  void shouldStorePrescribedRole() {
    for (Role role : Role.values()) {
      TenantMembership membership = new TenantMembership(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), role, Instant.now());
      assertThat(membership.role()).isEqualTo(role);
    }
  }

  @Test
  void shouldSupportAllFiveRoleValues() {
    assertThat(Role.values()).containsExactlyInAnyOrder(
        Role.VIEWER, Role.ANALYST, Role.MANAGER, Role.ADMIN, Role.SUPPORT
    );
  }
}
