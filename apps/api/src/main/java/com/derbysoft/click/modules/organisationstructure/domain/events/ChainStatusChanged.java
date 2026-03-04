package com.derbysoft.click.modules.organisationstructure.domain.events;

import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import java.time.Instant;
import java.util.UUID;

public record ChainStatusChanged(UUID chainId, ChainStatus oldStatus, ChainStatus newStatus, Instant changedAt) {}
