package com.lastmile.domain.port.in;

import com.lastmile.domain.model.UserRole;

public interface AuthUseCase {
    String login(String username, String password);
    String register(String username, String email, String password, UserRole role);
}