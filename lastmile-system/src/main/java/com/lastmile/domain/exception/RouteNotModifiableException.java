package com.lastmile.domain.exception;

import java.util.UUID;

public class RouteNotModifiableException extends DomainException {

    public RouteNotModifiableException(UUID routeId) {
        super("Route " + routeId + " is already in progress and cannot be modified.");
    }
}