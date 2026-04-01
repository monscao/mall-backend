package com.shopback.user.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        Boolean enabled,
        List<String> roleCodes
) {
}
