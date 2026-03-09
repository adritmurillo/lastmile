package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@With
public class BulkLoad {

    private final UUID id;
    private final String fileName;
    private final LoadSource loadSource;
    private final BulkLoadStatus status;

    private final int totalRecords;
    private final int successfulRecords;
    private final int failedRecords;

    private final List<LoadError> errors;

    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    private final String uploadedBy;

    public boolean hasErrors() {
        return failedRecords > 0;
    }

    public double getSuccessRate() {
        if (totalRecords == 0) return 0;
        return (double) successfulRecords / totalRecords * 100;
    }

    @Getter
    @Builder
    public static class LoadError {
        private final int rowNumber;
        private final String errorDescription;
        private final String problematicValue;
    }
}