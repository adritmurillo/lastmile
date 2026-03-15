package com.lastmile.application.usecase.dto;


import com.lastmile.domain.model.LoadSource;
import com.lastmile.domain.model.OrderPriority;
import com.lastmile.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrderDto {
    private final UUID id;
    private final String trackingCode;
    private final String externalTrackingCode;
    private final String platformOrderNumber;
    private final String recipientName;
    private final String recipientPhone;
    private final String recipientEmail;
    private final String addressText;
    private final Double latitude;
    private final Double longitude;
    private final Double weightKg;
    private final Double volumeCm3;
    private final OrderPriority priority;
    private final OrderStatus status;
    private final int deliveryAttempts;
    private final LocalDate deliveryDeadline;
    private final LocalDateTime createdAt;
    private final String notes;
    private final LoadSource loadSource;
}
