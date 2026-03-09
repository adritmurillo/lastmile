package com.lastmile.infrastructure.adapter.out.maps;

import com.lastmile.domain.model.Order;
import com.lastmile.domain.port.out.RouteOptimizerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class NearestNeighborRouteOptimizerAdapter implements RouteOptimizerPort {

    @Override
    public List<Order> optimizeDeliveryOrder(
            Double warehouseLatitude,
            Double warehouseLongitude,
            List<Order> orders) {

        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        if (orders.size() == 1) {
            return orders;
        }

        log.info("Optimizing route for {} orders using Nearest Neighbor algorithm", orders.size());

        List<Order> unvisited = new ArrayList<>(orders);
        List<Order> optimized = new ArrayList<>();

        double currentLat = warehouseLatitude;
        double currentLng = warehouseLongitude;

        while (!unvisited.isEmpty()) {
            Order nearest = findNearest(currentLat, currentLng, unvisited);
            optimized.add(nearest);
            unvisited.remove(nearest);

            currentLat = nearest.getLatitude() != null ? nearest.getLatitude() : currentLat;
            currentLng = nearest.getLongitude() != null ? nearest.getLongitude() : currentLng;
        }

        log.info("Route optimization completed. {} stops ordered.", optimized.size());
        return optimized;
    }

    private Order findNearest(double currentLat, double currentLng, List<Order> unvisited) {
        Order nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Order order : unvisited) {
            if (order.getLatitude() == null || order.getLongitude() == null) {
                continue;
            }

            double distance = haversineDistance(
                    currentLat, currentLng,
                    order.getLatitude(), order.getLongitude());

            if (distance < minDistance) {
                minDistance = distance;
                nearest = order;
            }
        }

        return nearest != null ? nearest : unvisited.get(0);
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}