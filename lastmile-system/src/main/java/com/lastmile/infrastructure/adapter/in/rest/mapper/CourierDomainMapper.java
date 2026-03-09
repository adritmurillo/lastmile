package com.lastmile.infrastructure.adapter.in.rest.mapper;

import com.lastmile.application.usecase.dto.CourierDto;
import com.lastmile.application.usecase.dto.VehicleDto;
import com.lastmile.domain.model.Courier;
import com.lastmile.domain.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourierDomainMapper {
    @Mapping(target = "fullName", expression = "java(courier.getFullName())")
    CourierDto toDto(Courier courier);
    Courier toDomain(CourierDto dto);
    List<CourierDto> toDtoList(List<Courier> couriers);
    VehicleDto toVehicleDto(Vehicle vehicle);
    Vehicle toVehicleDomain(VehicleDto dto);
    List<VehicleDto> toVehicleDtoList(List<Vehicle> vehicles);
}
