package com.lastmile.domain.exception;

import java.util.UUID;

public class StopNotFoundException extends DomainException {

    public StopNotFoundException(UUID id) {
        super("Stop not found with id: " + id);
    }
}