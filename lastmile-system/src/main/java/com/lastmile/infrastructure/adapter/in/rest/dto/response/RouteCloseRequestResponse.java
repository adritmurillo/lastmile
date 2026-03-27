package com.lastmile.infrastructure.adapter.in.rest.dto.response;

import com.lastmile.domain.model.CloseRequestStatus;
import com.lastmile.domain.model.RouteCloseReason;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RouteCloseRequestResponse {

    private final UUID id;
    private final UUID routeId;
    private final String routeCode;
    private final UUID courierId;
    private final String courierName;
    private final RouteCloseReason reason;
    private final String message;
    private final String photoUrl;
    private final CloseRequestStatus status;
    private final UUID reviewedBy;
    private final LocalDateTime reviewedAt;
    private final LocalDateTime createdAt;
    private final int pendingStopsCount;
}
