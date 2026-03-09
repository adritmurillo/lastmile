package com.lastmile.infrastructure.adapter.in.rest;

import com.lastmile.application.usecase.dto.RouteDto;
import com.lastmile.domain.port.in.DispatchUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.MoveOrderRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.RouteResponse;
import com.lastmile.infrastructure.adapter.in.rest.mapper.RouteDomainMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.RouteRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/dispatch")
@RequiredArgsConstructor
@Tag(name = "Dispatch", description = "Route planning and courier assignment")
public class DispatchController {

    private final DispatchUseCase dispatchUseCase;
    private final RouteDomainMapper routeDomainMapper;
    private final RouteRestMapper routeRestMapper;

    @GetMapping("/routes")
    @Operation(summary = "Get all routes for a specific date")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getRoutesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<RouteDto> routes = routeDomainMapper.toDtoList(
                dispatchUseCase.getRoutesByDate(date));

        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponseList(routes)));
    }

    @PostMapping("/proposal")
    @Operation(summary = "Generate assignment proposal",
            description = "Automatically distributes pending orders among available couriers")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> generateProposal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<RouteDto> routes = routeDomainMapper.toDtoList(
                dispatchUseCase.generateAssignmentProposal(date));

        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponseList(routes)));
    }

    @PatchMapping("/routes/move-order")
    @Operation(summary = "Move order between couriers",
            description = "Moves an order from one courier to another before confirming the route")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> moveOrder(
            @Valid @RequestBody MoveOrderRequest request) {

        RouteDto route = routeDomainMapper.toDto(
                dispatchUseCase.moveOrderBetweenCouriers(
                        request.getOrderId(),
                        request.getTargetCourierId()));

        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponse(route)));
    }

    @PostMapping("/routes/confirm")
    @Operation(summary = "Confirm and generate optimized routes",
            description = "Confirms the assignment and calculates optimal delivery order for each courier")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> confirmRoutes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<RouteDto> routes = routeDomainMapper.toDtoList(
                dispatchUseCase.confirmAndGenerateRoutes(date));

        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponseList(routes)));
    }

    @PatchMapping("/routes/{routeId}/reopen")
    @Operation(summary = "Reopen a confirmed route",
            description = "Allows modifications to a confirmed route if the courier hasn't left yet")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> reopenRoute(@PathVariable UUID routeId) {

        RouteDto route = routeDomainMapper.toDto(dispatchUseCase.reopenRoute(routeId));
        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponse(route)));
    }
}