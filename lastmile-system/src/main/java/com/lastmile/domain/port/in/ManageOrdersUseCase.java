package com.lastmile.domain.port.in;

import com.lastmile.domain.model.Order;
import com.lastmile.domain.model.OrderPriority;
import com.lastmile.domain.model.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManageOrdersUseCase {
    List<Order> getPendingOrders();
    List<Order> getOrders(OrderStatus status, LocalDate date, OrderPriority priority);
    Optional<Order> getOrderById(UUID id);
    Optional<Order> getOrderByTrackingCode(String trackingCode);
    int rescheduleFailedOrders();
    Order createOrder(Order order);
    Order cancelOrder(UUID orderId);
}
