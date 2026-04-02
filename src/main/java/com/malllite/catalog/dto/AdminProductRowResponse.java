package com.malllite.catalog.dto;

public record AdminProductRowResponse(
        Long id,
        String slug,
        String name,
        String subtitle,
        String brand,
        String categoryCode,
        String categoryName,
        String coverImage,
        String priceFrom,
        String priceTo,
        String marketPrice,
        String stockStatus,
        Boolean featured,
        Boolean onShelf
) {
}
