package com.shopback.auth.dto;

import java.util.List;

public record AuthUser(
        Long userId,
        String username,
        List<String> roleCodes
) {
}
