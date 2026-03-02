package com.derbysoft.click.modules.organisationstructure.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import com.derbysoft.click.modules.organisationstructure.domain.events.HierarchyChanged;
import com.derbysoft.click.modules.organisationstructure.domain.events.OrgNodeCreated;
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

    assertThat(pg.getEvents()).hasSize(1);
    assertThat(pg.getEvents().getFirst()).isInstanceOf(OrgNodeCreated.class);

    OrgNodeCreated event = (OrgNodeCreated) pg.getEvents().getFirst();
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

    // reconstitute so no OrgNodeCreated event is emitted
    PropertyGroup pg = PropertyGroup.reconstitute(id, oldParentId, "Test Group", "UTC", "USD", null, Instant.now(), Instant.now());
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
    PropertyGroup pg = PropertyGroup.reconstitute(UUID.randomUUID(), null, "Root", "UTC", "USD", null, Instant.now(), Instant.now());
    assertThat(pg.getEvents()).isEmpty();
  }

  @Test
  void shouldClearEventsAfterClearEvents() {
    PropertyGroup pg = PropertyGroup.create(UUID.randomUUID(), null, "Test", "UTC", "USD", null, Instant.now(), Instant.now());
    assertThat(pg.getEvents()).hasSize(1);
    pg.clearEvents();
    assertThat(pg.getEvents()).isEmpty();
  }

  @Test
  void shouldUpdateParentIdAfterMove() {
    UUID id = UUID.randomUUID();
    UUID oldParentId = UUID.randomUUID();
    UUID newParentId = UUID.randomUUID();

    PropertyGroup pg = PropertyGroup.reconstitute(id, oldParentId, "Test Group", "UTC", "USD", null, Instant.now(), Instant.now());
    assertThat(pg.getParentId()).isEqualTo(oldParentId);

    pg.move(newParentId);

    assertThat(pg.getParentId()).isEqualTo(newParentId);
  }
}
