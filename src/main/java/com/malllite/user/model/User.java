package com.malllite.user.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String username,
        String passwordHash,
        String nickname,
        String email,
        String phone,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
