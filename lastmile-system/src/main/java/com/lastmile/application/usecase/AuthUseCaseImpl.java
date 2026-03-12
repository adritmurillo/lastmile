package com.lastmile.application.usecase;

import com.lastmile.application.usecase.dto.AuthResponseDto;
import com.lastmile.domain.model.User;
import com.lastmile.domain.model.UserRole;
import com.lastmile.domain.port.in.AuthUseCase;
import com.lastmile.domain.port.out.CourierRepository;
import com.lastmile.domain.port.out.UserRepository;
import com.lastmile.infrastructure.adapter.out.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDto login(String username, String password) {
        log.info("Login attempt for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());

        UUID courierId = courierRepository.findByUserId(user.getId())
                .map(courier -> courier.getId())
                .orElse(null);

        log.info("Login successful for username: {}", username);
        return AuthResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .courierId(courierId)
                .build();
    }

    @Override
    public AuthResponseDto register(String username, String email, String password, UserRole role) {
        log.info("Registering new user: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", username);

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        return AuthResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .courierId(null)
                .build();
    }
}