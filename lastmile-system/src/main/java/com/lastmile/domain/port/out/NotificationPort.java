package com.lastmile.domain.port.out;

import com.lastmile.domain.model.Order;
import com.lastmile.domain.model.Route;
import com.lastmile.domain.model.Stop;

public interface NotificationPort {
    void notifyOrderCreated(Order order);
    void notifyOrderInTransit(Order order);
    void notifyOrderDelivered(Order order, Stop stop);
    void notifyOrderFailed(Order order, Stop stop);
    void notifyRouteClosed(Route route, int skippedStops);
}
