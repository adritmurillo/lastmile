package com.lastmile.domain.exception;

public class VehicleCapacityExceededException extends DomainException {

    public VehicleCapacityExceededException(String courierFullName) {
        super("Assigned orders exceed vehicle capacity for courier: " + courierFullName);
    }
}