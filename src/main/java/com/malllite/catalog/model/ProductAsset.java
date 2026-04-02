package com.malllite.catalog.model;

public record ProductAsset(
        Long id,
        Long productId,
        String imageUrl,
        String altText,
        Integer sortOrder
) {
}
