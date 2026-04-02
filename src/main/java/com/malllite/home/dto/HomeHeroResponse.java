package com.malllite.home.dto;

import com.malllite.catalog.dto.ProductCardResponse;

public record HomeHeroResponse(
        String eyebrow,
        String title,
        String subtitle,
        String backgroundStyle,
        ProductCardResponse product
) {
}
