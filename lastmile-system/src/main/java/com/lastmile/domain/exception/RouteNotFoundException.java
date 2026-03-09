package com.lastmile.domain.exception;

import java.util.UUID;

public class RouteNotFoundException extends DomainException {

    public RouteNotFoundException(UUID id) {
        super("Route not found with id: " + id);
    }
}