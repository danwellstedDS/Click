package com.derbysoft.click.modules.identityaccess.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.identityaccess.domain.aggregates.User;
import com.derbysoft.click.modules.identityaccess.domain.entities.TenantMembership;
import com.derbysoft.click.modules.identityaccess.domain.events.MembershipAdded;
import com.derbysoft.click.modules.identityaccess.domain.events.RoleAssigned;
import com.derbysoft.click.modules.identityaccess.domain.valueobjects.Role;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserAggregateTest {

  @Test
  void shouldEmitMembershipAddedOnAddMembership() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    UUID membershipId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Test User", true, Instant.now(), Instant.now());

    TenantMembership membership = user.addMembership(membershipId, tenantId, Role.MANAGER);

    assertThat(membership.tenantId()).isEqualTo(tenantId);
    assertThat(membership.role()).isEqualTo(Role.MANAGER);
    assertThat(user.getEvents()).hasSize(1);
    assertThat(user.getEvents().getFirst()).isInstanceOf(MembershipAdded.class);

    MembershipAdded event = (MembershipAdded) user.getEvents().getFirst();
    assertThat(event.membershipId()).isEqualTo(membershipId);
    assertThat(event.userId()).isEqualTo(userId);
    assertThat(event.tenantId()).isEqualTo(tenantId);
    assertThat(event.role()).isEqualTo(Role.MANAGER);
  }

  @Test
  void shouldEmitRoleAssignedOnRoleChange() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    UUID membershipId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Test User", true, Instant.now(), Instant.now());

    user.assignRole(membershipId, tenantId, Role.VIEWER, Role.ADMIN);

    assertThat(user.getEvents()).hasSize(1);
    assertThat(user.getEvents().getFirst()).isInstanceOf(RoleAssigned.class);

    RoleAssigned event = (RoleAssigned) user.getEvents().getFirst();
    assertThat(event.membershipId()).isEqualTo(membershipId);
    assertThat(event.userId()).isEqualTo(userId);
    assertThat(event.tenantId()).isEqualTo(tenantId);
    assertThat(event.previousRole()).isEqualTo(Role.VIEWER);
    assertThat(event.newRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  void shouldClearEventsAfterClearEvents() {
    UUID userId = UUID.randomUUID();
    User user = User.create(userId, "user@example.com", "hash", "Test User", true, Instant.now(), Instant.now());
    user.addMembership(UUID.randomUUID(), UUID.randomUUID(), Role.ADMIN);

    assertThat(user.getEvents()).hasSize(1);
    user.clearEvents();
    assertThat(user.getEvents()).isEmpty();
  }

  @Test
  void shouldAccumulateMultipleEvents() {
    User user = User.create(UUID.randomUUID(), "user@example.com", "hash", "Test User", true, Instant.now(), Instant.now());

    user.addMembership(UUID.randomUUID(), UUID.randomUUID(), Role.VIEWER);
    user.addMembership(UUID.randomUUID(), UUID.randomUUID(), Role.ADMIN);

    assertThat(user.getEvents()).hasSize(2);
    assertThat(user.getEvents().stream().filter(e -> e instanceof MembershipAdded).count()).isEqualTo(2);
  }
}
