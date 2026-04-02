package com.malllite.catalog.dto;

import java.util.List;

public record ProductCardResponse(
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
        String rating,
        Integer salesCount,
        String stockStatus,
        Boolean featured,
        List<String> tags
) {
}
