package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.OrderReturn;
import com.lastmile.infrastructure.adapter.out.persistence.entity.OrderReturnEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderReturnPersistenceMapper {
    
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "courier", ignore = true)
    @Mapping(target = "route", ignore = true)
    OrderReturnEntity toEntity(OrderReturn domain);
    
    @Mapping(target = "orderId", expression = "java(entity.getOrder() != null ? entity.getOrder().getId() : null)")
    @Mapping(target = "courierId", expression = "java(entity.getCourier() != null ? entity.getCourier().getId() : null)")
    @Mapping(target = "routeId", expression = "java(entity.getRoute() != null ? entity.getRoute().getId() : null)")
    OrderReturn toDomain(OrderReturnEntity entity);
    
    List<OrderReturn> toDomainList(List<OrderReturnEntity> entities);
}
