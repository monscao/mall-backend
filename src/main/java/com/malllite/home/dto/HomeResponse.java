package com.malllite.home.dto;

import com.malllite.catalog.dto.CategoryResponse;

import java.util.List;

public record HomeResponse(
        String theme,
        HomeHeroResponse hero,
        List<CategoryResponse> featuredCategories,
        List<HomeSectionResponse> sections
) {
}
