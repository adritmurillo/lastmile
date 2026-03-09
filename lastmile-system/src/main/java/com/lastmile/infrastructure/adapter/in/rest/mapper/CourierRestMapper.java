package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.CourierDto;
import com.lastmile.application.usecase.dto.VehicleDto;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.RegisterCourierRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.RegisterVehicleRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.CourierResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.VehicleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourierRestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CourierDto toDto(RegisterCourierRequest request);

    CourierResponse toResponse(CourierDto dto);

    List<CourierResponse> toResponseList(List<CourierDto> dtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    VehicleDto toVehicleDto(RegisterVehicleRequest request);

    VehicleResponse toVehicleResponse(VehicleDto dto);

    List<VehicleResponse> toVehicleResponseList(List<VehicleDto> dtos);

}
