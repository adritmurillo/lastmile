package com.lastmile.domain.port.out;

import com.lastmile.domain.model.Vehicle;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID id);
}
