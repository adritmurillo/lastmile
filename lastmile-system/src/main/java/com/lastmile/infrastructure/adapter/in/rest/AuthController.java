package com.lastmile.infrastructure.adapter.in.rest;

import com.lastmile.domain.model.UserRole;
import com.lastmile.domain.port.in.AuthUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @RequestBody Map<String, String> request) {
        String token = authUseCase.login(
                request.get("username"),
                request.get("password")
        );
        return ResponseEntity.ok(ApiResponse.ok(Map.of("token", token)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(
            @RequestBody Map<String, String> request) {
        String token = authUseCase.register(
                request.get("username"),
                request.get("email"),
                request.get("password"),
                UserRole.valueOf(request.get("role").toUpperCase())
        );
        return ResponseEntity.ok(ApiResponse.ok(Map.of("token", token)));
    }

}