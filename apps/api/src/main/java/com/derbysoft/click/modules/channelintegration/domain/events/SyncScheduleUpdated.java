package com.derbysoft.click.modules.channelintegration.domain.events;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import java.time.Instant;
import java.util.UUID;

public record SyncScheduleUpdated(
    UUID integrationId,
    SyncSchedule newSchedule,
    Instant occurredAt
) {}
