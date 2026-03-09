package com.lastmile.infrastructure.adapter.in.rest;

import com.lastmile.application.usecase.dto.CourierDto;
import com.lastmile.application.usecase.dto.VehicleDto;
import com.lastmile.domain.port.in.ManageCouriersUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.AssignVehicleRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.RegisterCourierRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.RegisterVehicleRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.CourierResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.VehicleResponse;
import com.lastmile.infrastructure.adapter.in.rest.mapper.CourierDomainMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.CourierRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
@Tag(name = "Couriers", description = "Courier and vehicle management")
public class CourierController {

    private final ManageCouriersUseCase manageCouriersUseCase;
    private final CourierDomainMapper courierDomainMapper;
    private final CourierRestMapper courierRestMapper;

    @GetMapping("/available")
    @Operation(summary = "Get available couriers for today")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<CourierResponse>>> getAvailableCouriers() {

        List<CourierDto> couriers = courierDomainMapper.toDtoList(
                manageCouriersUseCase.getAvailableCouriersToday());

        return ResponseEntity.ok(ApiResponse.ok(courierRestMapper.toResponseList(couriers)));
    }

    @PostMapping
    @Operation(summary = "Register a new courier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<CourierResponse>> registerCourier(
            @Valid @RequestBody RegisterCourierRequest request) {

        CourierDto dto = courierRestMapper.toDto(request);
        CourierDto saved = courierDomainMapper.toDto(
                manageCouriersUseCase.registerCourier(
                        courierDomainMapper.toDomain(dto)));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courierRestMapper.toResponse(saved)));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a courier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<CourierResponse>> activateCourier(@PathVariable UUID id) {

        CourierDto dto = courierDomainMapper.toDto(manageCouriersUseCase.activateCourier(id));
        return ResponseEntity.ok(ApiResponse.ok(courierRestMapper.toResponse(dto)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a courier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<CourierResponse>> deactivateCourier(@PathVariable UUID id) {

        CourierDto dto = courierDomainMapper.toDto(manageCouriersUseCase.deactivateCourier(id));
        return ResponseEntity.ok(ApiResponse.ok(courierRestMapper.toResponse(dto)));
    }

    @PatchMapping("/{id}/vehicle")
    @Operation(summary = "Assign a vehicle to a courier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<CourierResponse>> assignVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody AssignVehicleRequest request) {

        CourierDto dto = courierDomainMapper.toDto(
                manageCouriersUseCase.assignVehicle(id, request.getVehicleId()));

        return ResponseEntity.ok(ApiResponse.ok(courierRestMapper.toResponse(dto)));
    }

    @PostMapping("/vehicles")
    @Operation(summary = "Register a new vehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @Valid @RequestBody RegisterVehicleRequest request) {

        VehicleDto dto = courierRestMapper.toVehicleDto(request);
        VehicleDto saved = courierDomainMapper.toVehicleDto(
                manageCouriersUseCase.registerVehicle(
                        courierDomainMapper.toVehicleDomain(dto)));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courierRestMapper.toVehicleResponse(saved)));
    }

    @GetMapping
    @Operation(summary = "Get all couriers")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<CourierResponse>>> getAllCouriers() {
        List<CourierDto> couriers = courierDomainMapper.toDtoList(
                manageCouriersUseCase.getAllCouriers());
        return ResponseEntity.ok(ApiResponse.ok(courierRestMapper.toResponseList(couriers)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update courier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<CourierResponse>> updateCourier(
            @PathVariable UUID id,
            @Valid @RequestBody RegisterCourierRequest request) {

        CourierDto dto = CourierDto.builder()
                .id(id)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .documentNumber(request.getDocumentNumber())
                .phone(request.getPhone())
                .build();

        CourierDto saved = courierDomainMapper.toDto(
                manageCouriersUseCase.updateCourier(
                        courierDomainMapper.toDomain(dto)));

        return ResponseEntity.ok(ApiResponse.ok(courierRestMapper.toResponse(saved)));
    }

    @GetMapping("/vehicles")
    @Operation(summary = "Get all vehicles")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        List<VehicleDto> vehicles = courierDomainMapper.toVehicleDtoList(
                manageCouriersUseCase.getAllVehicles());
        return ResponseEntity.ok(ApiResponse.ok(courierRestMapper.toVehicleResponseList(vehicles)));
    }
}