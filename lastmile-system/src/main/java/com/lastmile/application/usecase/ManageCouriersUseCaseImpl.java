package com.lastmile.application.usecase;

import com.lastmile.domain.exception.CourierNotFoundException;
import com.lastmile.domain.exception.VehicleNotFoundException;
import com.lastmile.domain.model.Courier;
import com.lastmile.domain.model.CourierStatus;
import com.lastmile.domain.model.Vehicle;
import com.lastmile.domain.model.VehicleStatus;
import com.lastmile.domain.port.in.ManageCouriersUseCase;
import com.lastmile.domain.port.out.CourierRepository;
import com.lastmile.domain.port.out.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCouriersUseCaseImpl implements ManageCouriersUseCase {

    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public Courier registerCourier(Courier courier) {
        Courier newCourier = courier
                .withId(UUID.randomUUID())
                .withStatus(CourierStatus.ACTIVE)
                .withCreatedAt(LocalDateTime.now());

        log.info("Registering new courier: {}", newCourier.getFullName());
        return courierRepository.save(newCourier);
    }

    @Override
    @Transactional
    public Courier activateCourier(UUID courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        Courier activated = courier.withStatus(CourierStatus.ACTIVE);
        log.info("Activating courier: {}", courier.getFullName());
        return courierRepository.save(activated);
    }

    @Override
    @Transactional
    public Courier deactivateCourier(UUID courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        Courier deactivated = courier.withStatus(CourierStatus.INACTIVE);
        log.info("Deactivating courier: {}", courier.getFullName());
        return courierRepository.save(deactivated);
    }

    @Override
    public List<Courier> getAvailableCouriersToday() {
        return courierRepository.findAvailableToday();
    }

    @Override
    @Transactional
    public Vehicle registerVehicle(Vehicle vehicle) {
        Vehicle newVehicle = vehicle.withId(UUID.randomUUID())
                .withStatus(VehicleStatus.AVAILABLE);
        log.info("Registering new vehicle: {}", newVehicle.getLicensePlate());
        return vehicleRepository.save(newVehicle);
    }

    @Override
    @Transactional
    public Courier assignVehicle(UUID courierId, UUID vehicleId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        Courier updated = courier.withVehicle(vehicle);
        log.info("Assigning vehicle {} to courier {}",
                vehicle.getLicensePlate(), courier.getFullName());
        return courierRepository.save(updated);
    }

    @Override
    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    @Override
    @Transactional
    public Courier updateCourier(Courier courier) {
        Courier existing = courierRepository.findById(courier.getId())
                .orElseThrow(() -> new CourierNotFoundException(courier.getId()));

        Courier updated = existing
                .withFirstName(courier.getFirstName())
                .withLastName(courier.getLastName())
                .withDocumentNumber(courier.getDocumentNumber())
                .withPhone(courier.getPhone());

        log.info("Updating courier: {}", updated.getFullName());
        return courierRepository.save(updated);
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}