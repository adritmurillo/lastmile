package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanPickupRequest {

    @NotBlank(message = "Tracking code is required")
    private String trackingCode;
}
