package com.lastmile.application.usecase;

import com.lastmile.domain.exception.RouteCloseRequestNotFoundException;
import com.lastmile.domain.exception.RouteNotFoundException;
import com.lastmile.domain.model.*;
import com.lastmile.domain.port.in.ManageRouteCloseRequestUseCase;
import com.lastmile.domain.port.out.*;
import com.lastmile.infrastructure.adapter.out.websocket.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageRouteCloseRequestUseCaseImpl implements ManageRouteCloseRequestUseCase {

    private static final int SUSPENSION_THRESHOLD = 3;
    private static final int SUSPENSION_WINDOW_DAYS = 7;

    private final RouteCloseRequestRepository closeRequestRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final RouteRepository routeRepository;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RouteCloseRequest requestClose(UUID routeId, UUID courierId, RouteCloseReason reason,
                                          String message, String photoUrl) {
        // Validar que la ruta existe y pertenece al courier
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        if (!route.getCourier().getId().equals(courierId)) {
            throw new IllegalStateException("La ruta no pertenece al courier");
        }

        if (route.getStatus() != RouteStatus.IN_PROGRESS) {
            throw new IllegalStateException("Solo se puede solicitar cierre de rutas en progreso");
        }

        // Verificar si ya existe una solicitud pendiente
        RouteCloseRequest existing = closeRequestRepository.findByRouteId(routeId).orElse(null);
        if (existing != null && existing.getStatus() == CloseRequestStatus.PENDING) {
            throw new IllegalStateException("Ya existe una solicitud de cierre pendiente para esta ruta");
        }

        RouteCloseRequest request = RouteCloseRequest.builder()
                .id(UUID.randomUUID())
                .routeId(routeId)
                .courierId(courierId)
                .reason(reason)
                .message(message)
                .photoUrl(photoUrl)
                .status(CloseRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .courierName(route.getCourier().getFullName())
                .routeCode(route.getDate().toString() + "-" + route.getCourier().getFirstName())
                .pendingStopsCount((int) route.getStops().stream()
                        .filter(s -> s.getStatus() == StopStatus.PENDING)
                        .count())
                .build();

        RouteCloseRequest saved = closeRequestRepository.save(request);
        log.info("Route close request created: {} for route {} by courier {}. Reason: {}",
                saved.getId(), routeId, courierId, reason);

        // Notificar a los dispatchers en tiempo real
        notificationService.notifyRouteCloseRequested(
                saved.getId(),
                routeId,
                courierId,
                route.getCourier().getFullName(),
                reason.name(),
                saved.getPendingStopsCount()
        );

        return saved;
    }

    @Override
    public List<RouteCloseRequest> getPendingRequests() {
        List<RouteCloseRequest> requests = closeRequestRepository.findByStatus(CloseRequestStatus.PENDING);
        
        // Enrich with pending stops count from current route state
        List<RouteCloseRequest> enriched = new ArrayList<>();
        for (RouteCloseRequest req : requests) {
            Route route = routeRepository.findById(req.getRouteId()).orElse(null);
            if (route != null) {
                int pendingCount = (int) route.getStops().stream()
                        .filter(s -> s.getStatus() == StopStatus.PENDING)
                        .count();
                enriched.add(req.withPendingStopsCount(pendingCount));
            } else {
                enriched.add(req);
            }
        }
        
        return enriched;
    }

    @Override
    @Transactional
    public RouteCloseRequest approveRequest(UUID requestId, UUID dispatcherId) {
        RouteCloseRequest request = closeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RouteCloseRequestNotFoundException(requestId));

        if (request.getStatus() != CloseRequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RouteNotFoundException(request.getRouteId()));

        // 1. Update request status
        RouteCloseRequest approved = request
                .withStatus(CloseRequestStatus.APPROVED)
                .withReviewedBy(dispatcherId)
                .withReviewedAt(LocalDateTime.now());
        closeRequestRepository.save(approved);

        // 2. Get pending stops and their orders
        List<Stop> pendingStops = route.getStops().stream()
                .filter(s -> s.getStatus() == StopStatus.PENDING)
                .toList();

        // 3. Mark pending stops as SKIPPED
        for (Stop stop : pendingStops) {
            Stop skipped = stop.withStatus(StopStatus.SKIPPED);
            routeRepository.saveStop(skipped);
        }

        // 4. Mark orders as RETURNED_TO_WAREHOUSE and create OrderReturn records
        List<OrderReturn> orderReturns = new ArrayList<>();
        for (Stop stop : pendingStops) {
            Order order = stop.getOrder();
            if (order.getStatus() == OrderStatus.IN_TRANSIT || order.getStatus() == OrderStatus.PICKED_UP) {
                Order returned = order.withStatus(OrderStatus.RETURNED_TO_WAREHOUSE);
                orderRepository.save(returned);

                // Create OrderReturn record to track courier exclusion
                OrderReturn orderReturn = OrderReturn.builder()
                        .id(UUID.randomUUID())
                        .orderId(order.getId())
                        .courierId(request.getCourierId())
                        .routeId(route.getId())
                        .returnedAt(LocalDate.now())
                        .reason(request.getReason().name())
                        .build();
                orderReturns.add(orderReturn);
            }
        }
        orderReturnRepository.saveAll(orderReturns);

        // 5. Close the route
        Route closed = route
                .withStatus(RouteStatus.CLOSED)
                .withCompletedAt(LocalDateTime.now());
        routeRepository.save(closed);

        // 6. Check if courier should be suspended (3 approved closures in 7 days)
        LocalDateTime windowStart = LocalDateTime.now().minusDays(SUSPENSION_WINDOW_DAYS);
        long approvedCount = closeRequestRepository.countByCourierIdAndStatusAndCreatedAtAfter(
                request.getCourierId(), CloseRequestStatus.APPROVED, windowStart);

        if (approvedCount >= SUSPENSION_THRESHOLD) {
            Courier courier = courierRepository.findById(request.getCourierId()).orElse(null);
            if (courier != null && courier.getStatus() != CourierStatus.SUSPENDED) {
                Courier suspended = courier.withStatus(CourierStatus.SUSPENDED);
                courierRepository.save(suspended);
                log.warn("Courier {} suspended due to {} route closures in {} days",
                        courier.getFullName(), approvedCount, SUSPENSION_WINDOW_DAYS);
            }
        }

        log.info("Route close request {} approved by dispatcher {}. Route {} closed. {} packages returned.",
                requestId, dispatcherId, route.getId(), orderReturns.size());

        // Notificar al courier que su solicitud fue aprobada
        notificationService.notifyCloseRequestApproved(
                request.getCourierId(),
                route.getId(),
                orderReturns.size()
        );

        return approved;
    }

    @Override
    @Transactional
    public RouteCloseRequest rejectRequest(UUID requestId, UUID dispatcherId) {
        RouteCloseRequest request = closeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RouteCloseRequestNotFoundException(requestId));

        if (request.getStatus() != CloseRequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }

        RouteCloseRequest rejected = request
                .withStatus(CloseRequestStatus.REJECTED)
                .withReviewedBy(dispatcherId)
                .withReviewedAt(LocalDateTime.now());

        log.info("Route close request {} rejected by dispatcher {}. Courier must continue route.",
                requestId, dispatcherId);

        RouteCloseRequest saved = closeRequestRepository.save(rejected);
        
        // Notificar al courier que su solicitud fue rechazada
        notificationService.notifyCloseRequestRejected(
                request.getCourierId(),
                request.getRouteId()
        );

        return saved;
    }

    @Override
    public RouteCloseRequest getById(UUID requestId) {
        return closeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RouteCloseRequestNotFoundException(requestId));
    }

    @Override
    public RouteCloseRequest getPendingRequestForRoute(UUID routeId) {
        return closeRequestRepository.findByRouteId(routeId)
                .filter(req -> req.getStatus() == CloseRequestStatus.PENDING)
                .orElse(null);
    }
}
