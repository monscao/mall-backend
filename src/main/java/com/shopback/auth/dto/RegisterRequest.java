package com.shopback.auth.dto;

public record RegisterRequest(
        String username,
        String password,
        String nickname,
        String email,
        String phone
) {
}
