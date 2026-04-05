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
    List<String> getProofPhotoUrls(UUID orderId);
    Order markAsReadyToDispatch(UUID orderId);
    
    /**
     * Gets all orders that have been returned to warehouse due to early route closure.
     * These orders need to be re-scanned by dispatcher before they can be dispatched again.
     */
    List<Order> getReturnedToWarehouseOrders();
    
    /**
     * Confirms that a returned package has been received back at the warehouse.
     * Changes status from RETURNED_TO_WAREHOUSE to READY_TO_DISPATCH.
     *
     * @param orderId Order ID
     * @return Updated order
     */
    Order confirmReturnReceived(UUID orderId);
}
