package com.derbysoft.click.modules.organisationstructure.api.events;

import java.util.UUID;

public record PropertyCreated(UUID propertyId, UUID propertyGroupId, String name) {}
