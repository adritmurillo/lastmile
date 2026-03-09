package com.lastmile.infrastructure.adapter.in.rest.dto.response;

import com.lastmile.domain.model.RouteStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RouteResponse {

    private final UUID id;
    private final CourierResponse courier;
    private final LocalDate date;
    private final RouteStatus status;
    private final List<StopResponse> stops;
    private final Double totalWeightKg;
    private final Double totalVolumeCm3;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final int totalStops;
    private final long deliveredCount;
    private final long failedCount;
    private final long pendingCount;
    private final double completionPercentage;
}