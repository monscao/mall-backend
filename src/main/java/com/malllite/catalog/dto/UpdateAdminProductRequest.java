package com.malllite.catalog.dto;

public record UpdateAdminProductRequest(
        String categoryCode,
        String name,
        String subtitle,
        String slug,
        String brand,
        String priceFrom,
        String priceTo,
        String marketPrice,
        String stockStatus,
        Boolean featured,
        Boolean onShelf
) {
}
