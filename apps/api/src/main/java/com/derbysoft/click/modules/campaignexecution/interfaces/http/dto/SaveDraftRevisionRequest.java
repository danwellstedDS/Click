package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SaveDraftRevisionRequest(
    @NotEmpty List<PlanItemDraft> items
) {
    public record PlanItemDraft(
        String actionType,
        String resourceType,
        String resourceId,
        String payload,
        String applyOrder
    ) {}
}
