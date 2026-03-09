package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.RouteDto;
import com.lastmile.application.usecase.dto.StopDto;
import com.lastmile.domain.model.Route;
import com.lastmile.domain.model.Stop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderDomainMapper.class, CourierDomainMapper.class})
public interface RouteDomainMapper {

    @Mapping(target = "totalStops", expression = "java(route.getTotalStops())")
    @Mapping(target = "deliveredCount", expression = "java(route.getDeliveredCount())")
    @Mapping(target = "failedCount", expression = "java(route.getFailedCount())")
    @Mapping(target = "pendingCount", expression = "java(route.getPendingCount())")
    @Mapping(target = "completionPercentage", expression = "java(route.getCompletionPercentage())")
    RouteDto toDto(Route route);

    Route toDomain(RouteDto dto);

    List<RouteDto> toDtoList(List<Route> routes);

    StopDto toStopDto(Stop stop);

    Stop toStopDomain(StopDto dto);
}