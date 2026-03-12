package com.lastmile.domain.port.in;

import com.lastmile.application.usecase.dto.AuthResponseDto;
import com.lastmile.domain.model.UserRole;

public interface AuthUseCase {
    AuthResponseDto login(String username, String password);
    AuthResponseDto register(String username, String email, String password, UserRole role);

}