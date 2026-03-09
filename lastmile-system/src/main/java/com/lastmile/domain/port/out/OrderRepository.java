package com.lastmile.domain.port.out;

import com.lastmile.domain.model.Order;
import com.lastmile.domain.model.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    List<Order> saveAll(List<Order> orders);
    Optional<Order> findById(UUID id);
    Optional<Order> findByTrackingCode(String trackingCode);
    Optional<Order> findByExternalTrackingCode(String externalTrackingCode);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findPendingForDate(LocalDate date);
    List<Order> findReschedulableFailedOrders();
    boolean existsByExternalTrackingCode(String externalTrackingCode);
    List<Order> findAll();
}
