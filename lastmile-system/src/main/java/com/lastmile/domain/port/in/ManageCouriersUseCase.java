package com.lastmile.domain.port.in;

import com.lastmile.domain.model.Courier;
import com.lastmile.domain.model.Vehicle;

import java.util.List;
import java.util.UUID;

public interface ManageCouriersUseCase {
    Courier registerCourier(Courier courier);
    Courier activateCourier(UUID courierId);
    Courier deactivateCourier(UUID courierID);
    List<Courier> getAvailableCouriersToday();
    Vehicle registerVehicle(Vehicle vehicle);
    Courier assignVehicle(UUID courierId, UUID vehicleId);
    List<Courier> getAllCouriers();
    Courier updateCourier(Courier courier);
    List<Vehicle> getAllVehicles();
}
