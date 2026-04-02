package com.malllite.catalog.dto;

public record ProductAssetResponse(
        String imageUrl,
        String altText,
        Integer sortOrder
) {
}
