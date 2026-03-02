package com.derbysoft.click.modules.organisationstructure.domain.events;

import java.time.Instant;
import java.util.UUID;

public record PropertyCreated(UUID propertyId, UUID propertyGroupId, String name, Instant occurredAt) {}
