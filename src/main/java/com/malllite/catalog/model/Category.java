package com.malllite.catalog.model;

public record Category(
        Long id,
        String code,
        String name,
        String description,
        String icon,
        String bannerImage,
        Integer sortOrder,
        Boolean featured
) {
}
