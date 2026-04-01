package com.shopback.auth.service;

import com.shopback.auth.dto.AuthResponse;
import com.shopback.auth.dto.LoginRequest;
import com.shopback.auth.dto.RegisterRequest;
import com.shopback.user.model.User;
import com.shopback.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private static final String DEFAULT_CUSTOMER_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = userRepository.createUser(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.email(),
                request.phone()
        );

        userRepository.assignRoleByCode(user.id(), DEFAULT_CUSTOMER_ROLE);
        List<String> roleCodes = userRepository.findRoleCodesByUserIds(List.of(user.id()))
                .getOrDefault(user.id(), List.of());

        return buildAuthResponse(user, roleCodes);
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Username and password are required");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!Boolean.TRUE.equals(user.enabled())) {
            throw new IllegalArgumentException("User is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        List<String> roleCodes = userRepository.findRoleCodesByUserIds(List.of(user.id()))
                .getOrDefault(user.id(), List.of());

        return buildAuthResponse(user, roleCodes);
    }

    private AuthResponse buildAuthResponse(User user, List<String> roleCodes) {
        String token = jwtTokenService.generateToken(user.id(), user.username(), roleCodes);
        return new AuthResponse(user.id(), user.username(), token, roleCodes);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Username and password are required");
        }

        if (request.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
