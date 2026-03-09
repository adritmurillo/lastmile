package com.lastmile.application.usecase;

import com.lastmile.domain.exception.CourierNotFoundException;
import com.lastmile.domain.exception.OrderNotFoundException;
import com.lastmile.domain.exception.RouteNotFoundException;
import com.lastmile.domain.exception.RouteNotModifiableException;
import com.lastmile.domain.model.*;
import com.lastmile.domain.port.in.DispatchUseCase;
import com.lastmile.domain.port.out.*;
import com.lastmile.domain.service.RouteDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchUseCaseImpl implements DispatchUseCase {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final RouteRepository routeRepository;
    private final RouteOptimizerPort routeOptimizerPort;
    private final RouteDomainService routeDomainService;

    @Value("${lastmile.warehouse.latitude}")
    private Double warehouseLatitude;

    @Value("${lastmile.warehouse.longitude}")
    private Double warehouseLongitude;

    @Override
    @Transactional
    public List<Route> generateAssignmentProposal(LocalDate date) {
        log.info("Generating assignment proposal for date: {}", date);

        List<Order> pendingOrders = orderRepository.findPendingForDate(date);
        List<Courier> availableCouriers = courierRepository.findAvailableToday();

        log.info("Found {} pending orders and {} available couriers",
                pendingOrders.size(), availableCouriers.size());

        Map<UUID, List<Order>> assignment = routeDomainService
                .distributeOrdersAmongCouriers(pendingOrders, availableCouriers);

        return availableCouriers.stream()
                .filter(courier -> assignment.containsKey(courier.getId())
                        && !assignment.get(courier.getId()).isEmpty())
                .map(courier -> {
                    List<Order> courierOrders = assignment.get(courier.getId());
                    List<Stop> stops = routeDomainService.buildStopsFromOrders(courierOrders);
                    Route route = routeDomainService.buildRoute(courier, courierOrders, stops);

                    List<Stop> stopsWithIds = stops.stream()
                            .map(stop -> stop.withId(UUID.randomUUID()))
                            .collect(Collectors.toList());

                    Route routeWithIds = route
                            .withId(UUID.randomUUID())
                            .withDate(date)
                            .withStops(stopsWithIds);

                    return routeRepository.save(routeWithIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Route moveOrderBetweenCouriers(UUID orderId, UUID targetCourierId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Courier targetCourier = courierRepository.findById(targetCourierId)
                .orElseThrow(() -> new CourierNotFoundException(targetCourierId));

        Route targetRoute = routeRepository
                .findActiveCourierRoute(targetCourierId, LocalDate.now())
                .orElseThrow(() -> new RouteNotFoundException(targetCourierId));

        if (targetRoute.isInProgress()) {
            throw new RouteNotModifiableException(targetRoute.getId());
        }

        List<Stop> updatedStops = new java.util.ArrayList<>(targetRoute.getStops());
        Stop newStop = Stop.builder()
                .id(UUID.randomUUID())
                .order(order)
                .stopOrder(updatedStops.size() + 1)
                .status(StopStatus.PENDING)
                .build();

        updatedStops.add(newStop);

        Route updatedRoute = targetRoute
                .withStops(updatedStops)
                .withTotalWeightKg(targetRoute.getTotalWeightKg() + order.getWeightKg())
                .withTotalVolumeCm3(targetRoute.getTotalVolumeCm3() + order.getVolumeCm3());

        return routeRepository.save(updatedRoute);
    }

    @Override
    @Transactional
    public List<Route> confirmAndGenerateRoutes(LocalDate date) {
        log.info("Confirming and generating optimized routes for date: {}", date);

        List<Route> proposedRoutes = routeRepository.findByDate(date);

        List<Route> confirmedRoutes = proposedRoutes.stream()
                .map(route -> {
                    List<Order> orders = route.getStops().stream()
                            .map(Stop::getOrder)
                            .collect(Collectors.toList());

                    List<Order> optimizedOrders = routeOptimizerPort.optimizeDeliveryOrder(
                            warehouseLatitude, warehouseLongitude, orders);

                    List<Stop> optimizedStops = routeDomainService
                            .buildStopsFromOrders(optimizedOrders);

                    List<Order> assignedOrders = optimizedOrders.stream()
                            .map(order -> order.withStatus(OrderStatus.ASSIGNED))
                            .collect(Collectors.toList());

                    orderRepository.saveAll(assignedOrders);

                    return routeRepository.save(route
                            .withStops(optimizedStops)
                            .withDate(date)
                            .withStatus(RouteStatus.PENDING));
                })
                .collect(Collectors.toList());

        log.info("Successfully confirmed {} routes for date: {}", confirmedRoutes.size(), date);
        return confirmedRoutes;
    }

    @Override
    @Transactional
    public Route reopenRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        if (route.isInProgress()) {
            throw new RouteNotModifiableException(routeId);
        }

        Route reopened = route.withStatus(RouteStatus.PENDING);
        log.info("Reopening route: {} for courier: {}", routeId, route.getCourier().getFullName());
        return routeRepository.save(reopened);
    }

    @Override
    public List<Route> getRoutesByDate(LocalDate date) {
        return routeRepository.findByDate(date);
    }
}