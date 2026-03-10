package com.lastmile.application.usecase;

import com.lastmile.domain.model.*;
import com.lastmile.domain.port.in.ManageOrdersUseCase;
import com.lastmile.domain.port.out.NotificationPort;
import com.lastmile.domain.port.out.OrderRepository;
import com.lastmile.domain.port.out.RouteRepository;
import com.lastmile.domain.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageOrdersUseCaseImpl implements ManageOrdersUseCase {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final NotificationPort notificationPort;
    private final RouteRepository routeRepository;

    @Override
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    @Override
    public List<Order> getOrders(OrderStatus status, LocalDate date, OrderPriority priority) {
        List<Order> orders = status != null
                ? orderRepository.findByStatus(status)
                : orderRepository.findAll();

        return orders.stream()
                .filter(order -> date == null
                        || order.getDeliveryDeadline().equals(date))
                .filter(order -> priority == null
                        || order.getPriority() == priority)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> getOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> getOrderByTrackingCode(String trackingCode) {
        return orderRepository.findByTrackingCode(trackingCode);
    }

    @Override
    @Transactional
    public int rescheduleFailedOrders() {
        List<Order> failedOrders = orderRepository.findReschedulableFailedOrders();

        List<Order> rescheduled = orderDomainService
                .processFailedOrdersForRescheduling(failedOrders);

        orderRepository.saveAll(rescheduled);

        log.info("Rescheduled {} failed orders for next delivery attempt", rescheduled.size());

        return rescheduled.size();
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        Order newOrder = order
                .withId(UUID.randomUUID())
                .withTrackingCode(orderDomainService.generateTrackingCode())
                .withStatus(OrderStatus.PENDING)
                .withCreatedAt(LocalDateTime.now())
                .withLoadSource(order.getLoadSource() != null ? order.getLoadSource() : LoadSource.EXTERNAL_API);

        log.info("Creating new order: {}", newOrder.getTrackingCode());
        Order saved = orderRepository.save(newOrder);
        notificationPort.notifyOrderCreated(saved);
        return saved;
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }

        Order cancelled = order.withStatus(OrderStatus.CANCELLED);
        log.info("Cancelling order: {}", order.getTrackingCode());
        Order saved = orderRepository.save(cancelled);

        routeRepository.findRouteByOrderId(orderId).ifPresent(route -> {
            List<Stop> updatedStops = route.getStops().stream()
                    .filter(stop -> !stop.getOrder().getId().equals(orderId))
                    .collect(Collectors.toList());

            if (updatedStops.isEmpty()) {
                routeRepository.save(route
                        .withStops(updatedStops)
                        .withStatus(RouteStatus.CANCELLED));
                log.info("Route {} cancelled because it has no more stops", route.getId());
            } else {
                routeRepository.save(route.withStops(updatedStops));
            }
            log.info("Removed stop for cancelled order {} from route {}", order.getTrackingCode(), route.getId());
        });

        return saved;
    }
}