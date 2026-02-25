package domain;

import java.util.UUID;

public record PortfolioProperty(
    UUID id,
    UUID portfolioId,
    UUID propertyId
) {}
