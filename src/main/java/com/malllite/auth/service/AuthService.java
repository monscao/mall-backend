package com.malllite.auth.service;

import com.malllite.auth.dto.AuthResponse;
import com.malllite.auth.dto.LoginRequest;
import com.malllite.auth.dto.RegisterRequest;
import com.malllite.common.exception.BadRequestException;
import com.malllite.user.model.User;
import com.malllite.user.repository.UserRepository;
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
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
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
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!Boolean.TRUE.equals(user.enabled())) {
            throw new BadRequestException("User is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BadRequestException("Invalid username or password");
        }

        List<String> roleCodes = userRepository.findRoleCodesByUserIds(List.of(user.id()))
                .getOrDefault(user.id(), List.of());

        return buildAuthResponse(user, roleCodes);
    }

    private AuthResponse buildAuthResponse(User user, List<String> roleCodes) {
        String token = jwtTokenService.generateToken(user.id(), user.username(), roleCodes);
        return new AuthResponse(user.id(), user.username(), token, roleCodes);
    }
}
