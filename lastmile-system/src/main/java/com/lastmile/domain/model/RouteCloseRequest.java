package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@With
public class RouteCloseRequest {
    private final UUID id;
    private final UUID routeId;
    private final UUID courierId;
    private final RouteCloseReason reason;
    private final String message;
    private final String photoUrl;
    private final CloseRequestStatus status;
    private final UUID reviewedBy;
    private final LocalDateTime reviewedAt;
    private final LocalDateTime createdAt;
    
    // Datos adicionales para mostrar en UI (cargados desde joins)
    private final String courierName;
    private final String routeCode;
    private final int pendingStopsCount;
}
