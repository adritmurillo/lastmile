package com.lastmile.application.usecase.dto;

import com.lastmile.domain.model.FailureReason;
import com.lastmile.domain.model.StopStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class StopDto {

    private final UUID id;
    private final OrderDto order;
    private final int stopOrder;
    private final LocalDateTime estimatedArrival;
    private final LocalDateTime actualArrival;
    private final StopStatus status;
    private final FailureReason failureReason;
    private final String proofPhotoUrl;
}