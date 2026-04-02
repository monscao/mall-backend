package com.malllite.auth.dto;

import java.util.List;

public record AuthResponse(
        Long userId,
        String username,
        String token,
        List<String> roleCodes
) {
}
