package com.shopback.auth.service;

import com.shopback.auth.dto.AuthUser;
import com.shopback.auth.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;
    private final long tokenExpirationMinutes;

    public JwtTokenService(
            @Value("${auth.jwt-secret}") String jwtSecret,
            @Value("${auth.token-expiration-minutes}") long tokenExpirationMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.tokenExpirationMinutes = tokenExpirationMinutes;
    }

    public String generateToken(Long userId, String username, List<String> roleCodes) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(tokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roleCodes)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public AuthUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            List<String> roleCodes = Objects.requireNonNullElse(
                    claims.get("roles", List.class),
                    List.of()
            );

            if (userId == null || username == null || username.isBlank()) {
                throw new UnauthorizedException("Invalid token payload");
            }

            return new AuthUser(userId, username, roleCodes);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }
}
