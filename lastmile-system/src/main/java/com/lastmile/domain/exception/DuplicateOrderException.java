package com.lastmile.domain.exception;

public class DuplicateOrderException extends DomainException {

    public DuplicateOrderException(String externalTrackingCode) {
        super("An order with external tracking code " + externalTrackingCode + " already exists.");
    }
}