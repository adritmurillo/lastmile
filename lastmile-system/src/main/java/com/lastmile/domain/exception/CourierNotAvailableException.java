package com.lastmile.domain.exception;

import java.util.UUID;

public class CourierNotAvailableException extends DomainException {

    public CourierNotAvailableException(UUID courierId) {
        super("Courier " + courierId + " is not available today.");
    }
}