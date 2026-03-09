package com.lastmile.domain.exception;

import java.util.UUID;

public class BulkLoadNotFoundException extends DomainException {

    public BulkLoadNotFoundException(UUID id) {
        super("Bulk load not found with id: " + id);
    }
}