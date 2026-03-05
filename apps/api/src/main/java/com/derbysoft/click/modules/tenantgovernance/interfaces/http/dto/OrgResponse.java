package com.derbysoft.click.modules.tenantgovernance.interfaces.http.dto;

import java.util.UUID;

public record OrgResponse(UUID id, String name, String type) {}
