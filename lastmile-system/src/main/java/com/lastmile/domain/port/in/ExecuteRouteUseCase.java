package com.lastmile.domain.port.in;

import com.lastmile.domain.model.FailureReason;
import com.lastmile.domain.model.Route;
import com.lastmile.domain.model.Stop;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExecuteRouteUseCase {
    Route getMyRouteForToday(UUID courierId);
    Route startRoute(UUID routeId);
    Stop registerSuccessfulDelivery(UUID stopId, String proofPhotoUrl);
    Stop registerFailedDelivery(UUID stopId, FailureReason reason, String failureNotes);
    Route completeRoute(UUID routeId);
    List<Route> getRoutesByDate(LocalDate date);
    List<Stop> getPendingStopsFromPreviousDays(UUID courierId);

}
