package com.lastmile.domain.port.out;

import com.lastmile.domain.model.CloseRequestStatus;
import com.lastmile.domain.model.RouteCloseRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteCloseRequestRepository {
    RouteCloseRequest save(RouteCloseRequest request);
    
    Optional<RouteCloseRequest> findById(UUID id);
    
    Optional<RouteCloseRequest> findByRouteId(UUID routeId);
    
    List<RouteCloseRequest> findByStatus(CloseRequestStatus status);
    
    List<RouteCloseRequest> findByCourierIdAndCreatedAtAfter(UUID courierId, LocalDateTime since);
    
    long countByCourierIdAndStatusAndCreatedAtAfter(UUID courierId, CloseRequestStatus status, LocalDateTime since);
}
