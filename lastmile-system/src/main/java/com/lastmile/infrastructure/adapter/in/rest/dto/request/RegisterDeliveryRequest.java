package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterDeliveryRequest {

    @NotBlank(message = "Proof photo URL is required")
    private String proofPhotoUrl;
}