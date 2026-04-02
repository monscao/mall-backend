package com.malllite.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateProductRequest(
        @NotBlank(message = "Category is required")
        String categoryCode,
        @NotBlank(message = "Product name is required")
        String name,
        String subtitle,
        String slug,
        String brand,
        @NotBlank(message = "Cover image is required")
        String coverImage,
        String coverImageKey,
        String coverImageName,
        @NotBlank(message = "Price from is required")
        String priceFrom,
        @NotBlank(message = "Price to is required")
        String priceTo,
        @NotBlank(message = "Market price is required")
        String marketPrice,
        @NotBlank(message = "Stock status is required")
        String stockStatus,
        List<String> tags,
        String description,
        Boolean featured,
        Boolean onShelf,
        @Valid
        List<UploadedImageRequest> galleryImages,
        @NotEmpty(message = "At least one SKU is required")
        @Valid
        List<CreateProductSkuRequest> skus
) {
}
