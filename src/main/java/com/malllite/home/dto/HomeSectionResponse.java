package com.malllite.home.dto;

import com.malllite.catalog.dto.ProductCardResponse;

import java.util.List;

public record HomeSectionResponse(
        String code,
        String title,
        String subtitle,
        String layout,
        List<ProductCardResponse> products
) {
}
