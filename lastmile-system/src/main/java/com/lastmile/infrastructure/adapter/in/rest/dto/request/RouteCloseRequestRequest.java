package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import com.lastmile.domain.model.RouteCloseReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RouteCloseRequestRequest {

    @NotNull(message = "Route ID is required")
    private java.util.UUID routeId;

    @NotNull(message = "Close reason is required")
    private RouteCloseReason reason;

    @NotBlank(message = "Message is required")
    private String message;

    private String photoUrl;
}
