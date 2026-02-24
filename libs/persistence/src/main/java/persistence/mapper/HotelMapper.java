package persistence.mapper;

import domain.Hotel;
import persistence.entity.HotelEntity;

public final class HotelMapper {
  private HotelMapper() {
  }

  public static Hotel toDomain(HotelEntity entity) {
    return Hotel.create(
        entity.getId(),
        entity.getChainId(),
        entity.getName(),
        entity.isActive(),
        entity.getExternalHotelRef(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
