package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import com.lastmile.domain.model.OrderPriority;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "External tracking code is required")
    private String externalTrackingCode;

    private String platformOrderNumber;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Recipient phone is required")
    private String recipientPhone;

    @Email(message = "Invalid email format")
    private String recipientEmail;

    @NotBlank(message = "Delivery address is required")
    private String addressText;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be greater than zero")
    private Double weightKg;

    @NotNull(message = "Volume is required")
    @Positive(message = "Volume must be greater than zero")
    private Double volumeCm3;

    @NotNull(message = "Priority is required")
    private OrderPriority priority;

    @NotNull(message = "Delivery deadline is required")
    @Future(message = "Delivery deadline must be a future date")
    private LocalDate deliveryDeadline;

    private String notes;
}