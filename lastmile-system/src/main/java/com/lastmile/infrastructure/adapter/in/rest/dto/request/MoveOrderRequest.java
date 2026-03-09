package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class MoveOrderRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Target courier ID is required")
    private UUID targetCourierId;
}