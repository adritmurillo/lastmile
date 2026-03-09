package com.lastmile.infrastructure.adapter.in.rest.dto.response;

import com.lastmile.domain.model.CourierStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CourierResponse {

    private final UUID id;
    private final String fullName;
    private final String documentNumber;
    private final String phone;
    private final CourierStatus status;
    private final VehicleResponse vehicle;
    private final LocalDateTime createdAt;
}