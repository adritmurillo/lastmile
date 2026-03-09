package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.Route;
import com.lastmile.infrastructure.adapter.out.persistence.entity.RouteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CourierPersistenceMapper.class, StopPersistenceMapper.class})
public interface RoutePersistenceMapper {
    RouteEntity toEntity(Route route);
    Route toDomain(RouteEntity entity);
    List<Route> toDomainList(List<RouteEntity> entities);
}