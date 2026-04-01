package com.shopback.role.dto;

import java.util.List;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        List<String> permissionCodes
) {
}
