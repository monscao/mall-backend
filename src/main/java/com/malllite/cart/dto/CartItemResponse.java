package com.malllite.cart.dto;

public record CartItemResponse(
        Long id,
        String productSlug,
        String productName,
        String skuCode,
        String skuName,
        String specSummary,
        String coverImage,
        String salePrice,
        String marketPrice,
        Integer stock,
        Integer quantity,
        String lineTotal
) {
}
