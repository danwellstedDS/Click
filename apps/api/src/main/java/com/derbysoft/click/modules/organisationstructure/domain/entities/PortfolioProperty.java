package com.derbysoft.click.modules.organisationstructure.domain.entities;

import java.util.UUID;

public record PortfolioProperty(
    UUID id,
    UUID portfolioId,
    UUID propertyId
) {}
