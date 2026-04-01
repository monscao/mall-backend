package com.shopback.home.dto;

import com.shopback.catalog.dto.CategoryResponse;

import java.util.List;

public record HomeResponse(
        String theme,
        HomeHeroResponse hero,
        List<CategoryResponse> featuredCategories,
        List<HomeSectionResponse> sections
) {
}
