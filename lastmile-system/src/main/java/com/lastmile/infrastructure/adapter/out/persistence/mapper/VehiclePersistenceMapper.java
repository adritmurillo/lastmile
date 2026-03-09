package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.Vehicle;
import com.lastmile.infrastructure.adapter.out.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {
    VehicleEntity toEntity(Vehicle vehicle);
    Vehicle toDomain(VehicleEntity entity);
}
