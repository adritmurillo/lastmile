package com.lastmile.domain.service;

import com.lastmile.domain.exception.VehicleCapacityExceededException;
import com.lastmile.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RouteDomainService {
    public Map<UUID, List<Order>> distributeOrdersAmongCouriers(
            List<Order> orders,
            List<Courier> couriers
    ){
        List<Order> sortedOrders = orders.stream().sorted(Comparator.comparing(order ->
                order.getPriority() == OrderPriority.EXPRESS ? 0 : 1))
                .collect(Collectors.toList());

        Map<UUID, List<Order>> assignment = couriers.stream()
                .collect(Collectors.toMap(
                        Courier::getId,
                        courier -> new java.util.ArrayList<>()
                ));

        Map<UUID, Double> currentWeightPerCourier = couriers.stream()
                .collect(Collectors.toMap(Courier::getId, c -> 0.0));
        Map<UUID, Double> currentVolumePerCourier = couriers.stream()
                .collect(Collectors.toMap(Courier::getId, c -> 0.0));

        for (Order order : sortedOrders) {
            couriers.stream()
                    .filter(courier -> canCourierTakeOrder(
                            courier,
                            order,
                            currentWeightPerCourier.get(courier.getId()),
                            currentVolumePerCourier.get(courier.getId())))
                    .min(Comparator.comparingInt(courier -> assignment.get(courier.getId()).size()))
                    .ifPresent(courier -> {
                        assignment.get(courier.getId()).add(order);
                        currentWeightPerCourier.merge(courier.getId(), order.getWeightKg(), Double::sum);
                        currentVolumePerCourier.merge(courier.getId(), order.getVolumeCm3(), Double::sum);
                    });
        }

        return assignment;
    }

    public Route buildRoute(Courier courier, List<Order> orders, List<Stop> optimizedStops){
        double totalWeight = orders.stream()
                .mapToDouble(Order :: getWeightKg)
                .sum();

        double totalVolume = orders.stream()
                .mapToDouble(Order :: getVolumeCm3)
                .sum();

        Route route = Route.builder()
                .id(UUID.randomUUID())
                .courier(courier)
                .status(RouteStatus.PENDING)
                .stops(optimizedStops)
                .totalWeightKg(totalWeight)
                .totalVolumeCm3(totalVolume)
                .build();

        if (route.exceedsVehicleCapacity()){
            throw new VehicleCapacityExceededException(courier.getFullName());
        }
        return route;
    }

    public List<Stop> buildStopsFromOrders(List<Order> orderedOrders){
        int[] counter = {1};

        return orderedOrders.stream()
                .map(order -> Stop.builder()
                        .id(UUID.randomUUID())
                        .order(order)
                        .stopOrder(counter[0]++)
                        .status(StopStatus.PENDING)
                        .build())
                .collect(Collectors.toList());
    }

    private boolean canCourierTakeOrder(
            Courier courier,
            Order order,
            Double currentWeight,
            Double currentVolume
    ){
        if(!courier.isAvailableToday()) return false;
        if(courier.getVehicle() == null) return false;

        double newWeight = currentWeight + order.getWeightKg();
        double newVolume = currentVolume + order.getVolumeCm3();

        return courier.getVehicle().canHandle(newWeight, newVolume);
    }
}
