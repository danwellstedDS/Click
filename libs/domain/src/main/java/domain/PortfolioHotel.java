package domain;

import java.util.UUID;

public record PortfolioHotel(
    UUID id,
    UUID portfolioId,
    UUID hotelId
) {}
