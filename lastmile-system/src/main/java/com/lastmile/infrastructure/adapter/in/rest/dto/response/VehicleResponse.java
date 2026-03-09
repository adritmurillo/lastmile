package com.lastmile.infrastructure.adapter.in.rest.dto.response;

import com.lastmile.domain.model.VehicleStatus;
import com.lastmile.domain.model.VehicleType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class VehicleResponse {

    private final UUID id;
    private final String licensePlate;
    private final VehicleType type;
    private final Double maxWeightKg;
    private final Double maxVolumeCm3;
    private final VehicleStatus status;
}