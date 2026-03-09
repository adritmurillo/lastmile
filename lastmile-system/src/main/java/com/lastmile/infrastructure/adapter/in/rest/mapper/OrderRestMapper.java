package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.OrderDto;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.CreateOrderRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderRestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deliveryAttempts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "loadSource", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)

    OrderDto toDto(CreateOrderRequest request);
    OrderResponse toResponse(OrderDto dto);

    List<OrderResponse> toResponseList(List<OrderDto> dtos);

}
