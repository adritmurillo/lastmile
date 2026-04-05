package com.lastmile.infrastructure.adapter.in.rest;

import com.lastmile.application.usecase.dto.OrderDto;
import com.lastmile.application.usecase.dto.RouteDto;
import com.lastmile.application.usecase.dto.StopDto;
import com.lastmile.domain.port.in.ExecuteRouteUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.RegisterDeliveryRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.RegisterFailureRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.ScanPickupRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.PickupStatusResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.RouteResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.StopResponse;
import com.lastmile.infrastructure.adapter.in.rest.mapper.OrderDomainMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.OrderRestMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.RouteDomainMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.RouteRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Tag(name = "Route Execution", description = "Courier route execution and delivery registration")
public class RouteExecutionController {

    private final ExecuteRouteUseCase executeRouteUseCase;
    private final RouteDomainMapper routeDomainMapper;
    private final RouteRestMapper routeRestMapper;
    private final OrderDomainMapper orderDomainMapper;
    private final OrderRestMapper orderRestMapper;

    @GetMapping("/my-route")
    @Operation(summary = "Get courier's route for today",
            description = "Returns the full route with all stops ordered for delivery")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<RouteResponse>> getMyRouteForToday(
            @RequestParam UUID courierId) {

        RouteDto route = routeDomainMapper.toDto(
                executeRouteUseCase.getMyRouteForToday(courierId));

        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponse(route)));
    }

    @GetMapping
    @Operation(summary = "Get routes by date",
            description = "Returns all routes for a given date. Defaults to today if no date provided.")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getRoutesByDate(
            @RequestParam(required = false) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<RouteResponse> routes = executeRouteUseCase.getRoutesByDate(targetDate)
                .stream()
                .map(routeDomainMapper::toDto)
                .map(routeRestMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(routes));
    }

    @PostMapping("/{routeId}/pickup-scan")
    @Operation(summary = "Scan package for pickup",
            description = "Courier scans QR code to confirm package pickup. Changes order status to PICKED_UP.")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<OrderResponse>> scanPickup(
            @PathVariable UUID routeId,
            @Valid @RequestBody ScanPickupRequest request) {

        OrderDto order = orderDomainMapper.toDto(
                executeRouteUseCase.scanPickup(routeId, request.getTrackingCode()));

        return ResponseEntity.ok(ApiResponse.ok(orderRestMapper.toResponse(order)));
    }

    @GetMapping("/{routeId}/pickup-status")
    @Operation(summary = "Get pickup status",
            description = "Returns how many packages have been scanned for pickup")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<PickupStatusResponse>> getPickupStatus(
            @PathVariable UUID routeId) {

        var status = executeRouteUseCase.getPickupStatus(routeId);

        return ResponseEntity.ok(ApiResponse.ok(new PickupStatusResponse(
                status.totalPackages(),
                status.scannedPackages(),
                status.pendingPackages(),
                status.readyToStart()
        )));
    }

    @PostMapping("/{routeId}/start")
    @Operation(summary = "Start route",
            description = "Courier confirms they left the warehouse. All packages must be scanned first (PICKED_UP).")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<RouteResponse>> startRoute(@PathVariable UUID routeId) {

        RouteDto route = routeDomainMapper.toDto(executeRouteUseCase.startRoute(routeId));
        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponse(route)));
    }

    @PostMapping("/{routeId}/complete")
    @Operation(summary = "Complete route",
            description = "Courier confirms they returned to the warehouse. Route changes to COMPLETED.")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<RouteResponse>> completeRoute(@PathVariable UUID routeId) {

        RouteDto route = routeDomainMapper.toDto(executeRouteUseCase.completeRoute(routeId));
        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponse(route)));
    }

    @PostMapping("/stops/{stopId}/deliver")
    @Operation(summary = "Register successful delivery",
            description = "Courier marks a stop as delivered and uploads proof photo")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<StopResponse>> registerDelivery(
            @PathVariable UUID stopId,
            @Valid @RequestBody RegisterDeliveryRequest request) {

        StopDto stop = routeDomainMapper.toStopDto(
                executeRouteUseCase.registerSuccessfulDelivery(
                        stopId, request.getPhotoUrls()));

        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toStopResponse(stop)));
    }

    @PostMapping("/stops/{stopId}/fail")
    @Operation(summary = "Register failed delivery",
            description = "Courier marks a stop as failed with the reason. " +
                    "System automatically reschedules if attempts < 3.")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<StopResponse>> registerFailure(
            @PathVariable UUID stopId,
            @Valid @RequestBody RegisterFailureRequest request) {

        StopDto stop = routeDomainMapper.toStopDto(
                executeRouteUseCase.registerFailedDelivery(
                        stopId, request.getReason(), request.getFailureNotes()));

        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toStopResponse(stop)));
    }

    @GetMapping("/my-pending")
    @Operation(summary = "Get courier's pending stops from previous days")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<List<StopResponse>>> getMyPendingStops(
            @RequestParam UUID courierId) {
        List<StopResponse> stops = executeRouteUseCase.getPendingStopsFromPreviousDays(courierId)
                .stream()
                .map(routeDomainMapper::toStopDto)
                .map(routeRestMapper :: toStopResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(stops));
    }

    @PostMapping("/{routeId}/close")
    @Operation(summary = "Force close a route", description = "Dispatcher closes an incomplete route at end of day")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> closeRoute(
            @PathVariable UUID routeId,
            @RequestParam String reason) {

        RouteDto route = routeDomainMapper.toDto(executeRouteUseCase.closeRoute(routeId, reason));
        return ResponseEntity.ok(ApiResponse.ok(routeRestMapper.toResponse(route)));
    }

    @GetMapping("/my-history")
    @Operation(summary = "Get courier's delivery history from previous days")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getMyHistory(
            @RequestParam UUID courierId) {
        List<RouteResponse> routes = executeRouteUseCase.getCourierHistory(courierId)
                .stream()
                .map(routeDomainMapper::toDto)
                .map(routeRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(routes));
    }
}