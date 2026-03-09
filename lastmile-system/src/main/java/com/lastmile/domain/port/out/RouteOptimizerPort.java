package com.lastmile.domain.port.out;

import com.lastmile.domain.model.Order;
import org.aspectj.weaver.ast.Or;

import java.util.List;

public interface RouteOptimizerPort {
    List<Order> optimizeDeliveryOrder(
            Double warehouseLatitude,
            Double warehouseLongitude,
            List<Order> orders
    );
}
