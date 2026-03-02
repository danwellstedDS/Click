package com.derbysoft.click.modules.organisationstructure.domain.events;

import java.time.Instant;
import java.util.UUID;

public record HierarchyChanged(UUID nodeId, UUID oldParentId, UUID newParentId, Instant occurredAt) {}
