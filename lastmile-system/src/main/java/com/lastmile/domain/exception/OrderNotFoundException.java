package com.lastmile.domain.exception;

import java.util.UUID;

public class OrderNotFoundException extends DomainException {

    public OrderNotFoundException(UUID id) {
        super("Order not found with id: " + id);
    }

    public OrderNotFoundException(String trackingCode) {
        super("Order not found with tracking code: " + trackingCode);
    }
}