package com.malllite.catalog.dto;

public record CategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        String icon,
        String bannerImage,
        Boolean featured
) {
}
