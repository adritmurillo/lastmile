package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.Courier;
import com.lastmile.infrastructure.adapter.out.persistence.entity.CourierEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {VehiclePersistenceMapper.class})
public interface CourierPersistenceMapper {
    CourierEntity toEntity(Courier courier);
    Courier toDomain(CourierEntity entity);
}
