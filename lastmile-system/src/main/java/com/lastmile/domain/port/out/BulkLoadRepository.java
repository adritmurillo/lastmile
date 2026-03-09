package com.lastmile.domain.port.out;

import com.lastmile.domain.model.BulkLoad;

import java.util.Optional;
import java.util.UUID;

public interface BulkLoadRepository {
    BulkLoad save(BulkLoad bulkLoad);
    Optional<BulkLoad> findById(UUID id);
}
