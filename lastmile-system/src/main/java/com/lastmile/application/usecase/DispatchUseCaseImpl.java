package com.lastmile.application.usecase;

import com.lastmile.domain.exception.CourierNotFoundException;
import com.lastmile.domain.exception.OrderNotFoundException;
import com.lastmile.domain.exception.RouteNotFoundException;
import com.lastmile.domain.exception.RouteNotModifiableException;
import com.lastmile.domain.model.*;
import com.lastmile.domain.port.in.DispatchUseCase;
import com.lastmile.domain.port.out.*;
import com.lastmile.domain.service.RouteDomainService;
import com.lastmile.infrastructure.adapter.out.notification.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final PushNotificationService pushNotificationService;

    @Value("${lastmile.warehouse.latitude}")
    private Double warehouseLatitude;

    @Value("${lastmile.warehouse.longitude}")
    private Double warehouseLongitude;

    @Override
    @Transactional
    public List<Route> generateAssignmentProposal(LocalDate date) {
        log.info("Generating assignment proposal for date: {}", date);
        List<Route> existingRoutes = routeRepository.findByDate(date);
        List<Courier> availableCouriers = courierRepository.findAvailableToday();

        List<Order> basePendingOrders = orderRepository.findPendingForDate(date);

        List<Order> overdueOrders = availableCouriers.stream()
                .flatMap(courier -> routeRepository.findPendingStopsByCourier(courier.getId())
                        .stream()
                        .map(Stop::getOrder))
                .filter(order -> basePendingOrders.stream()
                        .noneMatch(o -> o.getId().equals(order.getId())))
                .toList();

        List<Order> allPendingOrders = new java.util.ArrayList<>(basePendingOrders);
        if (!overdueOrders.isEmpty()) {
            log.info("Including {} overdue orders from previous days", overdueOrders.size());
            allPendingOrders.addAll(overdueOrders);
        }

        if (!existingRoutes.isEmpty()) {
            Set<UUID> alreadyAssignedOrderIds = existingRoutes.stream()
                    .flatMap(r -> r.getStops().stream())
                    .map(stop -> stop.getOrder().getId())
                    .collect(java.util.stream.Collectors.toSet());

            List<Order> newOrders = allPendingOrders.stream()
                    .filter(o -> !alreadyAssignedOrderIds.contains(o.getId()))
                    .toList();

            if (newOrders.isEmpty()) {
                log.info("No new orders to assign for date: {}. Returning existing routes.", date);
                return existingRoutes;
            }

            log.info("Found {} new orders to add to existing routes for date: {}", newOrders.size(), date);

            Map<UUID, List<Order>> assignment = routeDomainService
                    .distributeOrdersAmongCouriers(newOrders, availableCouriers);

            List<Route> updatedRoutes = new java.util.ArrayList<>(existingRoutes);

            for (Route existingRoute : existingRoutes) {
                UUID courierId = existingRoute.getCourier().getId();
                List<Order> newCourierOrders = assignment.getOrDefault(courierId, List.of());

                if (!newCourierOrders.isEmpty()) {
                    List<Stop> currentStops = new java.util.ArrayList<>(existingRoute.getStops());
                    int nextStopOrder = currentStops.size() + 1;

                    for (Order order : newCourierOrders) {
                        Stop newStop = Stop.builder()
                                .id(UUID.randomUUID())
                                .order(order)
                                .stopOrder(nextStopOrder++)
                                .status(StopStatus.PENDING)
                                .build();
                        currentStops.add(newStop);
                    }

                    // Actualizar órdenes a ASSIGNED
                    List<Order> assignedOrders = newCourierOrders.stream()
                            .map(o -> o.withStatus(OrderStatus.ASSIGNED))
                            .collect(Collectors.toList());
                    orderRepository.saveAll(assignedOrders);

                    // Reactivar ruta si estaba COMPLETED
                    RouteStatus newStatus = existingRoute.getStatus() == RouteStatus.COMPLETED
                            || existingRoute.getStatus() == RouteStatus.IN_PROGRESS
                            ? existingRoute.getStatus()
                            : RouteStatus.CONFIRMED;

                    Route updated = routeRepository.save(existingRoute
                            .withStops(currentStops)
                            .withStatus(RouteStatus.CONFIRMED)
                            .withCompletedAt(null));

                    updatedRoutes.removeIf(r -> r.getId().equals(updated.getId()));
                    updatedRoutes.add(updated);
                    log.info("Added {} new stops to route of courier {}",
                            newCourierOrders.size(), existingRoute.getCourier().getFullName());
                }
            }

            return updatedRoutes;
        }

        log.info("Found {} pending orders and {} available couriers",
                allPendingOrders.size(), availableCouriers.size());

        Map<UUID, List<Order>> assignment = routeDomainService
                .distributeOrdersAmongCouriers(allPendingOrders, availableCouriers);

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
            throw new RouteNotModifiableException(
                    "El courier ya salió a ruta. Solo puedes asignar órdenes a couriers con estado Confirmado.");
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

        Order assignedOrder = order.withStatus(OrderStatus.ASSIGNED);
        orderRepository.save(assignedOrder);

        if (targetCourier.getFcmToken() != null){
            pushNotificationService.sendToDevice(
                    targetCourier.getFcmToken(),
                    "📦 Nueva entrega asignada",
                    "Se te ha asignado una nueva entrega: " + order.getRecipientName() +
                            " en " + order.getAddressText()
            );
        }

        return routeRepository.save(updatedRoute);
    }

    @Override
    @Transactional
    public List<Route> confirmAndGenerateRoutes(LocalDate date) {
        log.info("Confirming and generating optimized routes for date: {}", date);

        List<Route> proposedRoutes = routeRepository.findByDate(date);

        List<Route> confirmedRoutes = proposedRoutes.stream()
                .filter(route -> {
                    // Siempre procesar rutas PENDING o CONFIRMED
                    if (route.getStatus() == RouteStatus.PENDING ||
                            route.getStatus() == RouteStatus.CONFIRMED) return true;
                    // Para COMPLETED o IN_PROGRESS, solo procesar si tienen stops PENDING nuevos
                    return route.getStops().stream()
                            .anyMatch(s -> s.getStatus() == StopStatus.PENDING);
                })
                .map(route -> {
                    List<Stop> resetStops = route.getStops().stream()
                            .map(stop -> {
                                if (stop.getStatus() == StopStatus.FAILED
                                        && stop.getOrder().canBeRescheduled()) {
                                    return stop.withStatus(StopStatus.PENDING);
                                }
                                return stop;
                            })
                            .collect(Collectors.toList());

                    List<Stop> pendingStops = resetStops.stream()
                            .filter(s -> s.getStatus() == StopStatus.PENDING)
                            .collect(Collectors.toList());

                    List<Stop> nonPendingStops = resetStops.stream()
                            .filter(s -> s.getStatus() != StopStatus.PENDING)
                            .collect(Collectors.toList());

                    List<Order> pendingOrders = pendingStops.stream()
                            .map(Stop::getOrder)
                            .collect(Collectors.toList());

                    List<Order> optimizedOrders = routeOptimizerPort.optimizeDeliveryOrder(
                            warehouseLatitude, warehouseLongitude, pendingOrders);

                    List<Stop> optimizedStops = routeDomainService
                            .buildStopsFromOrders(optimizedOrders);

                    List<Order> assignedOrders = optimizedOrders.stream()
                            .map(order -> order.withStatus(OrderStatus.ASSIGNED))
                            .collect(Collectors.toList());

                    orderRepository.saveAll(assignedOrders);

                    List<Stop> allStops = new java.util.ArrayList<>(nonPendingStops);
                    allStops.addAll(optimizedStops);

                    // Mantener IN_PROGRESS si ya estaba iniciada, si no CONFIRMED
                    RouteStatus newStatus = route.getStatus() == RouteStatus.IN_PROGRESS
                            ? RouteStatus.IN_PROGRESS
                            : RouteStatus.CONFIRMED;

                    return routeRepository.save(route
                            .withStops(allStops)
                            .withDate(date)
                            .withStatus(newStatus)
                            .withCompletedAt(null));
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