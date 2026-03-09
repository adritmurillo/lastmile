package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Builder
@With
public class Stop {
    private final UUID id;
    private final UUID routeId;
    private final Order order;

    private final int stopOrder;

    private final LocalDateTime estimatedArrival;

    private final LocalDateTime actualArrival;

    private final StopStatus status;
    private final FailureReason failureReason;

    private final String proofPhotoUrl;

    public Stop markAsDelivered(LocalDateTime arrivalTime, String photoUrl){
        return this.withStatus(StopStatus.DELIVERED)
                .withActualArrival(arrivalTime)
                .withProofPhotoUrl(photoUrl);
    }

    public Stop markAsFailed(LocalDateTime arrivalTime, FailureReason reason){
        return this.withStatus(StopStatus.FAILED)
                .withActualArrival(arrivalTime)
                .withFailureReason(reason);
    }
}
