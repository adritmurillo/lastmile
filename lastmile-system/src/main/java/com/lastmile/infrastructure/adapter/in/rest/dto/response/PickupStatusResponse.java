package com.lastmile.infrastructure.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PickupStatusResponse {
    private final int totalPackages;
    private final int scannedPackages;
    private final int pendingPackages;
    private final boolean readyToStart;
}
