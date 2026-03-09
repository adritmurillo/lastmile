package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.OrderDto;
import com.lastmile.domain.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDomainMapper {
    OrderDto toDto(Order order);

    @Mapping(target = "deliveryAttempts", constant = "0")
    Order toDomain(OrderDto dto);

    List<OrderDto> toDtoList(List<Order> orders);
}
