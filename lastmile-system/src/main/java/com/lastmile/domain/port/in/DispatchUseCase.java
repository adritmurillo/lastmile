package com.lastmile.domain.port.in;

import com.lastmile.domain.model.Route;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DispatchUseCase {
    List<Route> generateAssignmentProposal(LocalDate date);
    Route moveOrderBetweenCouriers(UUID orderId, UUID targetCourierId);
    List<Route> confirmAndGenerateRoutes(LocalDate date);
    Route reopenRoute(UUID routeId);
    List<Route> getRoutesByDate(LocalDate date);
}
