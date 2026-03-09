package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.UUID;

@Getter
@Builder
@With
public class Vehicle {
    private final UUID id;
    private final String licensePlate;
    private final VehicleType type;

    private final Double maxWeightKg;
    private final Double maxVolumeCm3;

    private final VehicleStatus status;

    public boolean isAvailable(){
        return status == VehicleStatus.AVAILABLE;
    }

    public boolean canHandle(Double weightKg, Double volumeCm3){
        return weightKg <= maxWeightKg && volumeCm3 <= maxVolumeCm3;
    }
}
