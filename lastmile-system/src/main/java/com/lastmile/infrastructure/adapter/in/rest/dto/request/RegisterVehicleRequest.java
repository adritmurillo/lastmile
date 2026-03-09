package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import com.lastmile.domain.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterVehicleRequest {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;

    @NotNull(message = "Max weight is required")
    @Positive(message = "Max weight must be greater than zero")
    private Double maxWeightKg;

    @NotNull(message = "Max volume is required")
    @Positive(message = "Max volume must be greater than zero")
    private Double maxVolumeCm3;
}