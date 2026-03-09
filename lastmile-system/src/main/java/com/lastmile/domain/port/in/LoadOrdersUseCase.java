package com.lastmile.domain.port.in;

import com.lastmile.domain.model.BulkLoad;

import java.io.InputStream;
import java.util.UUID;

public interface LoadOrdersUseCase {
    UUID startFileLoad(InputStream file, String fileName, String uploadedBy);
    BulkLoad getBulkLoadStatus(UUID bulkLoadId);
}
