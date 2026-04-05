package com.lastmile.infrastructure.adapter.in.rest;

import com.lastmile.domain.model.RouteCloseRequest;
import com.lastmile.domain.port.in.ManageRouteCloseRequestUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.RouteCloseRequestRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.RouteCloseRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/route-close-requests")
@RequiredArgsConstructor
@Tag(name = "Route Close Requests", description = "Courier early route closure requests and dispatcher approval workflow")
public class RouteCloseRequestController {

    private final ManageRouteCloseRequestUseCase useCase;

    @PostMapping
    @Operation(summary = "Request early route closure",
            description = "Courier submits a request to close their route early. Requires dispatcher approval.")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<RouteCloseRequestResponse>> createRequest(
            @RequestParam UUID courierId,
            @Valid @RequestBody RouteCloseRequestRequest request) {

        RouteCloseRequest created = useCase.requestClose(
                request.getRouteId(),
                courierId,
                request.getReason(),
                request.getMessage(),
                request.getPhotoUrl()
        );

        return ResponseEntity.ok(ApiResponse.ok(toResponse(created)));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending close requests",
            description = "Dispatcher views all pending route closure requests awaiting approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<RouteCloseRequestResponse>>> getPendingRequests() {

        List<RouteCloseRequestResponse> requests = useCase.getPendingRequests()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(requests));
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get close request by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'COURIER')")
    public ResponseEntity<ApiResponse<RouteCloseRequestResponse>> getById(@PathVariable UUID requestId) {

        RouteCloseRequest request = useCase.getById(requestId);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(request)));
    }

    @GetMapping("/route/{routeId}/pending")
    @Operation(summary = "Get pending request for route",
            description = "Check if a route has a pending close request")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'COURIER')")
    public ResponseEntity<ApiResponse<RouteCloseRequestResponse>> getPendingForRoute(@PathVariable UUID routeId) {

        RouteCloseRequest request = useCase.getPendingRequestForRoute(routeId);
        if (request == null) {
            return ResponseEntity.ok(ApiResponse.ok(null));
        }
        return ResponseEntity.ok(ApiResponse.ok(toResponse(request)));
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve close request",
            description = "Dispatcher approves the closure request. Route is closed, pending packages returned to warehouse.")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteCloseRequestResponse>> approveRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID dispatcherId) {

        RouteCloseRequest approved = useCase.approveRequest(requestId, dispatcherId);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(approved)));
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject close request",
            description = "Dispatcher rejects the closure request. Courier must continue with the route.")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteCloseRequestResponse>> rejectRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID dispatcherId) {

        RouteCloseRequest rejected = useCase.rejectRequest(requestId, dispatcherId);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(rejected)));
    }

    private RouteCloseRequestResponse toResponse(RouteCloseRequest request) {
        return RouteCloseRequestResponse.builder()
                .id(request.getId())
                .routeId(request.getRouteId())
                .routeCode(request.getRouteCode())
                .courierId(request.getCourierId())
                .courierName(request.getCourierName())
                .reason(request.getReason())
                .message(request.getMessage())
                .photoUrl(request.getPhotoUrl())
                .status(request.getStatus())
                .reviewedBy(request.getReviewedBy())
                .reviewedAt(request.getReviewedAt())
                .createdAt(request.getCreatedAt())
                .pendingStopsCount(request.getPendingStopsCount())
                .build();
    }
}
