package com.shopback.permission.dto;

public record PermissionResponse(
        Long id,
        String code,
        String name,
        String description
) {
}
