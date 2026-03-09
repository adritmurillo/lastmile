package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class AssignVehicleRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;
}