package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@With
public class OrderReturn {
    private final UUID id;
    private final UUID orderId;
    private final UUID courierId;
    private final UUID routeId;
    private final LocalDate returnedAt;
    private final String reason;
}
