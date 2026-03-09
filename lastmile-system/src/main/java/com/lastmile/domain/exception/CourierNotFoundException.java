package com.lastmile.domain.exception;

import java.util.UUID;

public class CourierNotFoundException extends DomainException {

    public CourierNotFoundException(UUID id) {
        super("Courier not found with id: " + id);
    }
}