package com.lastmile.infrastructure.adapter.out.persistence.mapper;

import com.lastmile.domain.model.Order;
import com.lastmile.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderPersistenceMapper {
    OrderEntity toEntity(Order order);
    Order toDomain(OrderEntity entity);
    List<Order> toDomainList(List<OrderEntity> entities);
    List<OrderEntity> toEntityList(List<Order> orders);
}
