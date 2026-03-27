package com.lastmile.application.usecase;

import com.lastmile.domain.exception.OrderNotFoundException;
import com.lastmile.domain.exception.RouteNotFoundException;
import com.lastmile.domain.exception.StopNotFoundException;
import com.lastmile.domain.model.*;
import com.lastmile.domain.port.in.ExecuteRouteUseCase;
import com.lastmile.domain.port.out.NotificationPort;
import com.lastmile.domain.port.out.OrderRepository;
import com.lastmile.domain.port.out.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecuteRouteUseCaseImpl implements ExecuteRouteUseCase {

    private final RouteRepository routeRepository;
    private final OrderRepository orderRepository;
    private final NotificationPort notificationPort;

    @Override
    public List<Route> getCourierHistory(UUID courierId) {
        return routeRepository.findCompletedByCourier(courierId);
    }

    @Override
    public Route getMyRouteForToday(UUID courierId) {
        return routeRepository.findActiveCourierRoute(courierId, LocalDate.now())
                .orElseThrow(() -> new RouteNotFoundException(courierId));
    }

    @Override
    @Transactional
    public Order scanPickup(UUID routeId, String trackingCode) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        // Buscar el stop con este trackingCode en la ruta
        Stop matchingStop = route.getStops().stream()
                .filter(stop -> stop.getOrder().getTrackingCode().equals(trackingCode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El paquete " + trackingCode + " no pertenece a esta ruta"));

        Order order = matchingStop.getOrder();

        // Validar estado actual
        if (order.getStatus() == OrderStatus.PICKED_UP) {
            log.info("Order {} already picked up, skipping", trackingCode);
            return order;
        }

        if (order.getStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "El paquete " + trackingCode + " no está en estado ASSIGNED. Estado actual: " + order.getStatus());
        }

        // Cambiar a PICKED_UP
        Order pickedUp = order.withStatus(OrderStatus.PICKED_UP);
        Order saved = orderRepository.save(pickedUp);

        log.info("Order {} scanned and picked up by courier for route {}",
                trackingCode, routeId);

        return saved;
    }

    @Override
    public PickupStatus getPickupStatus(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        int total = route.getStops().size();
        int scanned = (int) route.getStops().stream()
                .filter(stop -> stop.getOrder().getStatus() == OrderStatus.PICKED_UP
                        || stop.getOrder().getStatus() == OrderStatus.IN_TRANSIT
                        || stop.getOrder().getStatus() == OrderStatus.DELIVERED
                        || stop.getOrder().getStatus() == OrderStatus.FAILED)
                .count();
        int pending = total - scanned;

        return new PickupStatus(total, scanned, pending, pending == 0);
    }

    @Override
    @Transactional
    public Route startRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        if (route.getStatus() == RouteStatus.IN_PROGRESS || route.getStatus() == RouteStatus.COMPLETED) {
            log.info("Route {} already started, skipping", routeId);
            return route;
        }

        // Verificar que todos los paquetes estén escaneados (PICKED_UP)
        List<Order> notPickedUp = route.getStops().stream()
                .map(Stop::getOrder)
                .filter(order -> order.getStatus() == OrderStatus.ASSIGNED)
                .toList();

        if (!notPickedUp.isEmpty()) {
            String missing = notPickedUp.stream()
                    .map(Order::getTrackingCode)
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "No puedes iniciar la ruta. Faltan escanear " + notPickedUp.size() +
                            " paquetes: " + missing);
        }

        // Cambiar todos los PICKED_UP an IN_TRANSIT
        List<Order> inTransitOrders = route.getStops().stream()
                .map(Stop::getOrder)
                .filter(order -> order.getStatus() == OrderStatus.PICKED_UP)
                .map(order -> order.withStatus(OrderStatus.IN_TRANSIT))
                .collect(Collectors.toList());

        orderRepository.saveAll(inTransitOrders);
        inTransitOrders.forEach(notificationPort::notifyOrderInTransit);

        Route started = route
                .withStatus(RouteStatus.IN_PROGRESS)
                .withStartedAt(LocalDateTime.now());

        log.info("Route {} started by courier: {}. {} packages in transit.",
                routeId, route.getCourier().getFullName(), inTransitOrders.size());

        return routeRepository.save(started);
    }

    @Override
    @Transactional
    public Stop registerSuccessfulDelivery(UUID stopId, List<String> photoUrls) {
        Stop stop = routeRepository.findStopById(stopId)
                .orElseThrow(() -> new StopNotFoundException(stopId));

        // Validar estado del stop
        if (stop.getStatus() != StopStatus.PENDING) {
            throw new IllegalStateException(
                    "El stop ya fue procesado. Estado actual: " + stop.getStatus());
        }

        // Validar estado de la orden
        Order order = stop.getOrder();
        if (order.getStatus() != OrderStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "La orden debe estar EN TRÁNSITO para ser entregada. Estado actual: " + order.getStatus());
        }

        String firstPhotoUrl = photoUrls != null && !photoUrls.isEmpty() ? photoUrls.getFirst() : null;
        Stop delivered = stop.markAsDelivered(LocalDateTime.now(), firstPhotoUrl);

        Order deliveredOrder = order.markAsDelivered();
        orderRepository.save(deliveredOrder);

        Stop savedStop = routeRepository.saveStop(delivered);

        if (photoUrls != null && !photoUrls.isEmpty()) {
            routeRepository.saveStopPhotos(savedStop.getId(), photoUrls);
        }

        notificationPort.notifyOrderDelivered(deliveredOrder, delivered);

        log.info("Order {} successfully delivered at stop {} with {} photos",
                order.getTrackingCode(), stopId, photoUrls != null ? photoUrls.size() : 0);

        routeRepository.findById(stop.getRouteId()).ifPresent(route -> {
            boolean allDone = route.getStops().stream()
                    .allMatch(s -> s.getId().equals(stopId) || s.getStatus() != StopStatus.PENDING);
            if (allDone) {
                Route completed = route.withStatus(RouteStatus.COMPLETED).withCompletedAt(LocalDateTime.now());
                routeRepository.save(completed);
                log.info("Route {} auto-completed.", route.getId());
            }
        });

        return savedStop;
    }

    @Override
    @Transactional
    public Stop registerFailedDelivery(UUID stopId, FailureReason reason, String failureNotes) {
        Stop stop = routeRepository.findStopById(stopId)
                .orElseThrow(() -> new StopNotFoundException(stopId));

        // Validar estado del stop
        if (stop.getStatus() != StopStatus.PENDING) {
            throw new IllegalStateException(
                    "El stop ya fue procesado. Estado actual: " + stop.getStatus());
        }

        // Validar estado de la orden
        Order order = stop.getOrder();
        if (order.getStatus() != OrderStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "La orden debe estar EN TRÁNSITO para marcarla como fallida. Estado actual: " + order.getStatus());
        }

        Stop failed = stop.markAsFailed(LocalDateTime.now(), reason, failureNotes);

        Order failedOrder = order.recordFailure();
        orderRepository.save(failedOrder);
        notificationPort.notifyOrderFailed(failedOrder, failed);

        log.info("Delivery failed for order {} at stop {}. Reason: {}. Notes: {}. Attempts: {}/3",
                order.getTrackingCode(), stopId, reason, failureNotes, failedOrder.getDeliveryAttempts());

        return routeRepository.saveStop(failed);
    }

    @Override
    @Transactional
    public Route completeRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        Route completed = route
                .withStatus(RouteStatus.COMPLETED)
                .withCompletedAt(LocalDateTime.now());

        log.info("Route {} completed. Delivered: {}/{} stops",
                routeId, route.getDeliveredCount(), route.getTotalStops());

        return routeRepository.save(completed);
    }

    @Override
    public List<Route> getRoutesByDate(LocalDate date) {
        log.info("Getting routes for date: {}", date);
        return routeRepository.findByDate(date);
    }

    @Override
    public List<Stop> getPendingStopsFromPreviousDays(UUID courierId) {
        return routeRepository.findPendingStopsByCourier(courierId);
    }

    @Override
    @Transactional
    public Route closeRoute(UUID routeId, String reason) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        log.info("closeRoute: Route {} has {} stops", routeId, route.getStops().size());

        // Obtener stops pendientes
        List<Stop> pendingStops = route.getStops().stream()
                .filter(s -> s.getStatus() == StopStatus.PENDING)
                .toList();

        log.info("closeRoute: Found {} pending stops", pendingStops.size());

        // Marcar órdenes IN_TRANSIT/PICKED_UP/ASSIGNED como SKIPPED (cierre manual no cuenta como intento)
        List<Order> ordersToSkip = pendingStops.stream()
                .map(Stop::getOrder)
                .filter(order -> order.getStatus() == OrderStatus.IN_TRANSIT 
                              || order.getStatus() == OrderStatus.PICKED_UP
                              || order.getStatus() == OrderStatus.ASSIGNED)
                .map(order -> order.withStatus(OrderStatus.RETURNED_TO_WAREHOUSE))
                .toList();

        if (!ordersToSkip.isEmpty()) {
            orderRepository.saveAll(ordersToSkip);
            log.info("Marked {} orders as RETURNED_TO_WAREHOUSE due to route manual close", ordersToSkip.size());
        }

        // Marcar stops pendientes como SKIPPED
        for (Stop stop : pendingStops) {
            Stop skipped = stop.withStatus(StopStatus.SKIPPED);
            log.info("closeRoute: Saving stop {} as SKIPPED", stop.getId());
            routeRepository.saveStop(skipped);
        }

        Route closed = route
                .withStatus(RouteStatus.CLOSED)
                .withCompletedAt(LocalDateTime.now());

        log.info("Route {} closed by dispatcher. Reason: {}. {} stops marked as SKIPPED",
                routeId, reason, pendingStops.size());

        return routeRepository.save(closed);
    }

    @Override
    @Transactional
    public int autoCloseIncompleteRoutes() {
        log.info("=== AUTO-CLOSE: Starting automatic route closing ===");
        
        List<Route> incompleteRoutes = routeRepository.findIncompleteRoutes();
        log.info("Found {} incomplete routes to close", incompleteRoutes.size());
        
        int closedCount = 0;
        
        for (Route route : incompleteRoutes) {
            try {
                closeRouteAutomatically(route);
                closedCount++;
            } catch (Exception e) {
                log.error("Failed to auto-close route {}: {}", route.getId(), e.getMessage(), e);
            }
        }
        
        log.info("=== AUTO-CLOSE: Closed {} routes ===", closedCount);
        return closedCount;
    }

    private void closeRouteAutomatically(Route route) {
        log.info("Auto-closing route {} (courier: {}, date: {}, status: {})",
                route.getId(), 
                route.getCourier() != null ? route.getCourier().getFullName() : "N/A",
                route.getDate(),
                route.getStatus());

        // Obtener stops pendientes
        List<Stop> pendingStops = route.getStops().stream()
                .filter(s -> s.getStatus() == StopStatus.PENDING)
                .toList();

        // Marcar órdenes como SKIPPED (cierre automático no cuenta como intento)
        List<Order> ordersToSkip = pendingStops.stream()
                .map(Stop::getOrder)
                .filter(order -> order.getStatus() == OrderStatus.IN_TRANSIT 
                              || order.getStatus() == OrderStatus.PICKED_UP
                              || order.getStatus() == OrderStatus.ASSIGNED)
                .map(order -> order.withStatus(OrderStatus.SKIPPED))
                .toList();

        if (!ordersToSkip.isEmpty()) {
            orderRepository.saveAll(ordersToSkip);
            log.info("Route {}: Marked {} orders as SKIPPED", route.getId(), ordersToSkip.size());
        }

        // Marcar stops pendientes como SKIPPED
        for (Stop stop : pendingStops) {
            Stop skipped = stop.withStatus(StopStatus.SKIPPED);
            routeRepository.saveStop(skipped);
        }

        // Cerrar la ruta
        Route closed = route
                .withStatus(RouteStatus.CLOSED)
                .withCompletedAt(LocalDateTime.now());

        routeRepository.save(closed);
        
        // Notificar
        notificationPort.notifyRouteClosed(closed, pendingStops.size());

        log.info("Route {} auto-closed. {} stops marked as SKIPPED",
                route.getId(), pendingStops.size());
    }
}