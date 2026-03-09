package com.lastmile.domain.exception;

import java.util.UUID;

public class VehicleNotFoundException extends DomainException {

    public VehicleNotFoundException(UUID id) {
        super("Vehicle not found with id: " + id);
    }
}