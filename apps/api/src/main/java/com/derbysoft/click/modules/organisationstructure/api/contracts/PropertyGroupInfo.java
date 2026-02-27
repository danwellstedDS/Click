package com.derbysoft.click.modules.organisationstructure.api.contracts;

import java.util.UUID;

public record PropertyGroupInfo(UUID id, String name, UUID primaryOrgId) {}
