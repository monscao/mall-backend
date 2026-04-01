package com.shopback.permission.model;

import java.time.LocalDateTime;

public record Permission(
        Long id,
        String code,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
