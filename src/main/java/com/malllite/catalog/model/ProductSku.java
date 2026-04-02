package com.malllite.catalog.model;

public record ProductSku(
        Long id,
        Long productId,
        String skuCode,
        String name,
        String specSummary,
        String salePrice,
        String marketPrice,
        Integer stock,
        String coverImage,
        Boolean isDefault
) {
}
