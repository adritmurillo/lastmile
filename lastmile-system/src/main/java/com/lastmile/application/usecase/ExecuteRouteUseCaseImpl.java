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

        List<Order> inTransitOrders = route.getStops().stream()
                .map(Stop::getOrder)
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
    public Stop registerSuccessfulDelivery(UUID stopId, String proofPhotoUrl) {
        Stop stop = routeRepository.findStopById(stopId)
                .orElseThrow(() -> new StopNotFoundException(stopId));

        Stop delivered = stop.markAsDelivered(LocalDateTime.now(), proofPhotoUrl);

        Order deliveredOrder = stop.getOrder().markAsDelivered();
        orderRepository.save(deliveredOrder);
        notificationPort.notifyOrderDelivered(deliveredOrder);

        log.info("Order {} successfully delivered at stop {}",
                stop.getOrder().getTrackingCode(), stopId);

        return routeRepository.saveStop(delivered);
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
}