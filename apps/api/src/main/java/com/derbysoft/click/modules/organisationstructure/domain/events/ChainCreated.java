package com.derbysoft.click.modules.organisationstructure.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ChainCreated(UUID chainId, String name, Instant createdAt) {}
