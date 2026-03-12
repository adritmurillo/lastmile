package com.lastmile.application.usecase.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthResponseDto {
    private String token;
    private String username;
    private String role;
    private UUID courierId;
}
