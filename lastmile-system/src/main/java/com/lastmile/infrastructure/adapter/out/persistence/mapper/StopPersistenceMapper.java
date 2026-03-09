package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.Stop;
import com.lastmile.infrastructure.adapter.out.persistence.entity.StopEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderPersistenceMapper.class})
public interface StopPersistenceMapper {
    @Mapping(target = "route", ignore = true)
    StopEntity toEntity(Stop stop);

    @Mapping(target = "routeId", expression = "java(entity.getRoute() != null ? entity.getRoute().getId() : null)")
    Stop toDomain(StopEntity entity);
}