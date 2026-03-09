package com.lastmile.application.usecase.dto;

import com.lastmile.domain.model.CourierStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CourierDto {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private final String documentNumber;
    private final String phone;
    private final CourierStatus status;
    private final VehicleDto vehicle;
    private final LocalDateTime createdAt;
}