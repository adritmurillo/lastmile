package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class User {
    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final UserRole role;
    private final boolean active;
    private final LocalDateTime createdAt;
}
