package com.derbysoft.click.modules.organisationstructure.domain.events;

import java.time.Instant;
import java.util.UUID;

public record OrgNodeCreated(UUID propertyGroupId, String name, UUID parentId, Instant occurredAt) {}
