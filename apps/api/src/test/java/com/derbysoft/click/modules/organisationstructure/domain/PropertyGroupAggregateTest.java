package com.derbysoft.click.modules.organisationstructure.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.domain.events.ChainCreated;
import com.derbysoft.click.modules.organisationstructure.domain.events.ChainStatusChanged;
import com.derbysoft.click.modules.organisationstructure.domain.events.HierarchyChanged;
import com.derbysoft.click.modules.organisationstructure.domain.events.OrgNodeCreated;
import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PropertyGroupAggregateTest {

  @Test
  void shouldEmitOrgNodeCreatedOnCreate() {
    UUID id = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    Instant now = Instant.now();

    PropertyGroup pg = PropertyGroup.create(id, parentId, "Test Group", "UTC", "USD", null, now, now);

    assertThat(pg.getEvents()).hasSize(2);
    assertThat(pg.getEvents().get(0)).isInstanceOf(OrgNodeCreated.class);

    OrgNodeCreated event = (OrgNodeCreated) pg.getEvents().get(0);
    assertThat(event.propertyGroupId()).isEqualTo(id);
    assertThat(event.name()).isEqualTo("Test Group");
    assertThat(event.parentId()).isEqualTo(parentId);
    assertThat(event.occurredAt()).isEqualTo(now);
  }

  @Test
  void shouldEmitHierarchyChangedOnMove() {
    UUID id = UUID.randomUUID();
    UUID oldParentId = UUID.randomUUID();
    UUID newParentId = UUID.randomUUID();

    PropertyGroup pg = PropertyGroup.reconstitute(id, oldParentId, "Test Group", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.ACTIVE);
    assertThat(pg.getEvents()).isEmpty();

    pg.move(newParentId);

    assertThat(pg.getEvents()).hasSize(1);
    assertThat(pg.getEvents().getFirst()).isInstanceOf(HierarchyChanged.class);

    HierarchyChanged event = (HierarchyChanged) pg.getEvents().getFirst();
    assertThat(event.nodeId()).isEqualTo(id);
    assertThat(event.oldParentId()).isEqualTo(oldParentId);
    assertThat(event.newParentId()).isEqualTo(newParentId);
  }

  @Test
  void shouldNotEmitEventsOnReconstitute() {
    PropertyGroup pg = PropertyGroup.reconstitute(UUID.randomUUID(), null, "Root", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.ACTIVE);
    assertThat(pg.getEvents()).isEmpty();
  }

  @Test
  void shouldClearEventsAfterClearEvents() {
    PropertyGroup pg = PropertyGroup.create(UUID.randomUUID(), null, "Test", "UTC", "USD", null, Instant.now(), Instant.now());
    assertThat(pg.getEvents()).hasSize(2);
    pg.clearEvents();
    assertThat(pg.getEvents()).isEmpty();
  }

  @Test
  void shouldUpdateParentIdAfterMove() {
    UUID id = UUID.randomUUID();
    UUID oldParentId = UUID.randomUUID();
    UUID newParentId = UUID.randomUUID();

    PropertyGroup pg = PropertyGroup.reconstitute(id, oldParentId, "Test Group", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.ACTIVE);
    assertThat(pg.getParentId()).isEqualTo(oldParentId);

    pg.move(newParentId);

    assertThat(pg.getParentId()).isEqualTo(newParentId);
  }

  @Test
  void shouldDefaultStatusToActiveOnCreate() {
    PropertyGroup pg = PropertyGroup.create(UUID.randomUUID(), null, "New Chain", "UTC", "USD", null, Instant.now(), Instant.now());
    assertThat(pg.getStatus()).isEqualTo(ChainStatus.ACTIVE);
  }

  @Test
  void shouldTransitionToInactiveOnDeactivate() {
    PropertyGroup pg = PropertyGroup.reconstitute(UUID.randomUUID(), null, "Chain", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.ACTIVE);
    pg.deactivate();
    assertThat(pg.getStatus()).isEqualTo(ChainStatus.INACTIVE);
  }

  @Test
  void shouldThrowConflictWhenDeactivatingAlreadyInactive() {
    PropertyGroup pg = PropertyGroup.reconstitute(UUID.randomUUID(), null, "Chain", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.INACTIVE);
    assertThatThrownBy(pg::deactivate).isInstanceOf(DomainError.Conflict.class);
  }

  @Test
  void shouldEmitChainStatusChangedEvent() {
    UUID id = UUID.randomUUID();
    PropertyGroup pg = PropertyGroup.reconstitute(id, null, "Chain", "UTC", "USD", null, Instant.now(), Instant.now(), ChainStatus.ACTIVE);
    pg.deactivate();

    assertThat(pg.getEvents()).hasSize(1);
    ChainStatusChanged event = (ChainStatusChanged) pg.getEvents().getFirst();
    assertThat(event.chainId()).isEqualTo(id);
    assertThat(event.oldStatus()).isEqualTo(ChainStatus.ACTIVE);
    assertThat(event.newStatus()).isEqualTo(ChainStatus.INACTIVE);
  }

  @Test
  void shouldEmitChainCreatedOnCreate() {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    PropertyGroup pg = PropertyGroup.create(id, null, "My Chain", "UTC", "USD", null, now, now);

    assertThat(pg.getEvents().get(1)).isInstanceOf(ChainCreated.class);
    ChainCreated event = (ChainCreated) pg.getEvents().get(1);
    assertThat(event.chainId()).isEqualTo(id);
    assertThat(event.name()).isEqualTo("My Chain");
  }
}
