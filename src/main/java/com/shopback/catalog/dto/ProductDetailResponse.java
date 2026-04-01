package com.shopback.catalog.dto;

import java.util.List;

public record ProductDetailResponse(
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
        String description,
        List<String> tags,
        List<ProductSkuResponse> skus
) {
}
