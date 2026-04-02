package com.malllite.catalog.dto;

public record ProductSkuResponse(
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
