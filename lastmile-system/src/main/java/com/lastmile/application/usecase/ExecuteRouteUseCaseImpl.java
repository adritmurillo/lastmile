package com.lastmile.application.usecase;

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

    @Override
    public List<Route> getCourierHistory(UUID courierId) {
        return routeRepository.findCompletedByCourier(courierId);
    }

    private final OrderRepository orderRepository;
    private final NotificationPort notificationPort;


    @Override
    public Route getMyRouteForToday(UUID courierId) {
        return routeRepository.findActiveCourierRoute(courierId, LocalDate.now())
                .orElseThrow(() -> new RouteNotFoundException(courierId));
    }

    @Override
    @Transactional
    public Route startRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        if (route.getStatus() == RouteStatus.IN_PROGRESS || route.getStatus()==RouteStatus.COMPLETED){
            log.info("Route {} already started, skipping", routeId);
            return route;
        }

        List<Order> inTransitOrders = route.getStops().stream()
                .map(Stop::getOrder)
                .filter(order -> order.getStatus() == OrderStatus.ASSIGNED)
                .map(order -> order.withStatus(OrderStatus.IN_TRANSIT))
                .collect(Collectors.toList());

        orderRepository.saveAll(inTransitOrders);
        inTransitOrders.forEach(notificationPort::notifyOrderInTransit);

        Route started = route
                .withStatus(RouteStatus.IN_PROGRESS)
                .withStartedAt(LocalDateTime.now());

        log.info("Route {} started by courier: {}", routeId, route.getCourier().getFullName());
        return routeRepository.save(started);
    }

    @Override
    @Transactional
    public Stop registerSuccessfulDelivery(UUID stopId, List<String> photoUrls) {
        Stop stop = routeRepository.findStopById(stopId)
                .orElseThrow(() -> new StopNotFoundException(stopId));

        String firstPhotoUrl = photoUrls != null && !photoUrls.isEmpty() ? photoUrls.getFirst() : null;
        Stop delivered = stop.markAsDelivered(LocalDateTime.now(), firstPhotoUrl);

        Order deliveredOrder = stop.getOrder().markAsDelivered();
        orderRepository.save(deliveredOrder);

        Stop savedStop = routeRepository.saveStop(delivered);

        if (photoUrls != null && !photoUrls.isEmpty()) {
            routeRepository.saveStopPhotos(savedStop.getId(), photoUrls);
        }

        notificationPort.notifyOrderDelivered(deliveredOrder, delivered);

        log.info("Order {} successfully delivered at stop {} with {} photos",
                stop.getOrder().getTrackingCode(), stopId, photoUrls != null ? photoUrls.size() : 0);

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

        Stop failed = stop.markAsFailed(LocalDateTime.now(), reason, failureNotes);

        Order failedOrder = stop.getOrder().recordFailure();
        orderRepository.save(failedOrder);
        notificationPort.notifyOrderFailed(failedOrder, failed);

        log.info("Delivery failed for order {} at stop {}. Reason: {}. Notes: {}. Attempts: {}/3",
                stop.getOrder().getTrackingCode(), stopId, reason, failureNotes, failedOrder.getDeliveryAttempts());

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

        long pendingCount = route.getStops().stream()
                .filter(s -> s.getStatus() == StopStatus.PENDING)
                .count();

        Route closed = route
                .withStatus(RouteStatus.COMPLETED)
                .withCompletedAt(LocalDateTime.now());

        log.info("Route {} force-closed by dispatcher. Reason: {}. Pending stops left: {}",
                routeId, reason, pendingCount);

        return routeRepository.save(closed);
    }
}