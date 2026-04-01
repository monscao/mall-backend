package com.shopback.role.model;

import java.time.LocalDateTime;

public record Role(
        Long id,
        String code,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
