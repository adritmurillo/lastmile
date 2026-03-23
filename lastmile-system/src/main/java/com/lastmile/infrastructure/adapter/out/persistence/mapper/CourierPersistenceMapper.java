package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.Courier;
import com.lastmile.infrastructure.adapter.out.persistence.entity.CourierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {VehiclePersistenceMapper.class})
public interface CourierPersistenceMapper {
    @Mapping(target = "vehicle", source = "vehicle")
    CourierEntity toEntity(Courier courier);

    @Mapping(target = "vehicle", source = "vehicle")
    Courier toDomain(CourierEntity entity);
}
