package com.shopback.home.dto;

import com.shopback.catalog.dto.ProductCardResponse;

public record HomeHeroResponse(
        String eyebrow,
        String title,
        String subtitle,
        String backgroundStyle,
        ProductCardResponse product
) {
}
