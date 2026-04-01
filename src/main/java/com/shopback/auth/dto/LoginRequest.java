package com.shopback.auth.dto;

public record LoginRequest(
        String username,
        String password
) {
}
