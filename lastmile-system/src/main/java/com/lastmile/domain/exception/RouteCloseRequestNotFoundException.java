package com.lastmile.domain.exception;

import java.util.UUID;

public class RouteCloseRequestNotFoundException extends DomainException {

    public RouteCloseRequestNotFoundException(UUID id) {
        super("Route close request not found with id: " + id);
    }
}
