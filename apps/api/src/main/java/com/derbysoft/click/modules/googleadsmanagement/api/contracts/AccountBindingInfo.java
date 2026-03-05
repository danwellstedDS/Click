package com.derbysoft.click.modules.googleadsmanagement.api.contracts;

import java.util.UUID;

public record AccountBindingInfo(UUID id, UUID tenantId, String customerId, String status, String bindingType) {}
