package com.lastmile.domain.port.out;

import com.lastmile.domain.model.Route;
import com.lastmile.domain.model.Stop;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteRepository {
    Route save(Route route);
    Optional<Route> findById(UUID id);
    Optional<Route> findActiveCourierRoute(UUID courierId, LocalDate date);
    List<Route> findByDate(LocalDate date);
    Stop saveStop(Stop stop);
    Optional<Stop> findStopById(UUID id);
    Optional<Route> findRouteByOrderId(UUID orderId);
    List<Route> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Stop> findPendingStopsByCourier(UUID courierId);
}
