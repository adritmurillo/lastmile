package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@With
public class Order {
    private static final int MAX_DELIVERY_ATTEMPTS = 3;
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

    public boolean canBeRescheduled(){
        return deliveryAttempts < MAX_DELIVERY_ATTEMPTS;
    }

    public boolean hasCoordinates(){
        return latitude != null && longitude != null;
    }

    public Order markAsDelivered(){
        return this.withStatus(OrderStatus.DELIVERED);
    }

    public Order recordFailure(){
        int newAttempts = this.deliveryAttempts + 1;
        OrderStatus newStatus = newAttempts >= MAX_DELIVERY_ATTEMPTS
                ? OrderStatus.RETURNED
                : OrderStatus.FAILED;

        return this
                .withDeliveryAttempts(newAttempts)
                .withStatus(newStatus);
    }

    public Order reschedule() {
        if (!canBeRescheduled()) {
            throw new IllegalStateException(
                    "Order " + trackingCode + " cannot be rescheduled. " +
                            "Maximum delivery attempts (" + MAX_DELIVERY_ATTEMPTS + ") reached."
            );
        }
        return this.withStatus(OrderStatus.PENDING);
    }

}
