package com.lastmile.domain.model;


import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@With
public class Courier {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String documentNumber;
    private final String phone;
    private final CourierStatus status;
    private final Vehicle vehicle;
    private final LocalDateTime createdAt;

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public boolean isAvailableToday(){
        return status == CourierStatus.ACTIVE;
    }

    public boolean canCarry(Double weightKg, Double volumeCm3){
        if (vehicle == null) return false;
        return vehicle.canHandle(weightKg, volumeCm3);
    }
}
