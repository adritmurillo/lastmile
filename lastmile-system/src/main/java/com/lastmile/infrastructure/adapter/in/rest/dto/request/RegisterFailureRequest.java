package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import com.lastmile.domain.model.FailureReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterFailureRequest {

    @NotNull(message = "Failure reason is required")
    private FailureReason reason;
}