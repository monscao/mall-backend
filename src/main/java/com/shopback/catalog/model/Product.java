package com.shopback.catalog.model;

public record Product(
        Long id,
        Long categoryId,
        String categoryCode,
        String categoryName,
        String name,
        String subtitle,
        String slug,
        String brand,
        String coverImage,
        String priceFrom,
        String priceTo,
        String marketPrice,
        String rating,
        Integer salesCount,
        String stockStatus,
        String tags,
        String description,
        Boolean featured,
        Boolean onShelf
) {
}
