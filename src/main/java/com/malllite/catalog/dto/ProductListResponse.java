package com.malllite.catalog.dto;

import java.util.List;

public record ProductListResponse(
        List<ProductCardResponse> items,
        Integer page,
        Integer size,
        Long total,
        Integer totalPages,
        Boolean hasPrevious,
        Boolean hasNext,
        String keyword
) {
}
