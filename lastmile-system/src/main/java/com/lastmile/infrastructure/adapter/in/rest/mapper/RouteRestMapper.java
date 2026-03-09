package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.RouteDto;
import com.lastmile.application.usecase.dto.StopDto;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.RouteResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.StopResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderRestMapper.class, CourierRestMapper.class})
public interface RouteRestMapper {
    RouteResponse toResponse(RouteDto dto);
    List<RouteResponse> toResponseList(List<RouteDto> dtos);
    StopResponse toStopResponse(StopDto dto);
}
