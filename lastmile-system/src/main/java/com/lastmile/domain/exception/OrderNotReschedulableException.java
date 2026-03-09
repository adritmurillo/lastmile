package com.lastmile.domain.exception;

public class OrderNotReschedulableException extends DomainException {

    public OrderNotReschedulableException(String trackingCode, int attempts) {
        super("Order " + trackingCode + " cannot be rescheduled. " +
                "Maximum delivery attempts reached: " + attempts);
    }
}