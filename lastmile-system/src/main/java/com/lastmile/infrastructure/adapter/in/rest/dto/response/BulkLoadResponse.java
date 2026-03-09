package com.lastmile.infrastructure.adapter.in.rest.dto.response;

import com.lastmile.domain.model.BulkLoad;
import com.lastmile.domain.model.BulkLoadStatus;
import com.lastmile.domain.model.LoadSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class BulkLoadResponse {

    private final UUID id;
    private final String fileName;
    private final LoadSource loadSource;
    private final BulkLoadStatus status;
    private final int totalRecords;
    private final int successfulRecords;
    private final int failedRecords;
    private final double successRate;
    private final List<BulkLoad.LoadError> errors;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final String uploadedBy;
}