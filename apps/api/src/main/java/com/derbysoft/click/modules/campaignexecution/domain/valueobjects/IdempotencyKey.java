package com.derbysoft.click.modules.campaignexecution.domain.valueobjects;

import java.util.UUID;

public record IdempotencyKey() {

    public static String compute(UUID revisionId, UUID itemId,
                                  WriteActionType actionType, int targetVersion) {
        return revisionId + ":" + itemId + ":" + actionType.name() + ":" + targetVersion;
    }
}
