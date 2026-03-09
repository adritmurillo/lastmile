package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@With
public class Route {

    private final UUID id;
    private final Courier courier;
    private final LocalDate date;
    private final RouteStatus status;
    private final List<Stop> stops;

    private final Double totalWeightKg;

    private final Double totalVolumeCm3;

    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;

    public int getTotalStops() {
        return stops != null ? stops.size() : 0;
    }

    public long getDeliveredCount() {
        if (stops == null) return 0;
        return stops.stream()
                .filter(s -> s.getStatus() == StopStatus.DELIVERED)
                .count();
    }

    public long getFailedCount() {
        if (stops == null) return 0;
        return stops.stream()
                .filter(s -> s.getStatus() == StopStatus.FAILED)
                .count();
    }

    public long getPendingCount() {
        if (stops == null) return 0;
        return stops.stream()
                .filter(s -> s.getStatus() == StopStatus.PENDING)
                .count();
    }

    public boolean exceedsVehicleCapacity() {
        Vehicle vehicle = courier.getVehicle();
        if (vehicle == null) return true;
        return totalWeightKg > vehicle.getMaxWeightKg()
                || totalVolumeCm3 > vehicle.getMaxVolumeCm3();
    }

    public boolean isInProgress() {
        return status == RouteStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == RouteStatus.COMPLETED;
    }

    public double getCompletionPercentage() {
        if (getTotalStops() == 0) return 0;
        return (double) getDeliveredCount() / getTotalStops() * 100;
    }
}