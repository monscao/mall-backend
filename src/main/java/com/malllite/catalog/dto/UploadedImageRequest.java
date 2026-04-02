package com.malllite.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadedImageRequest(
        String key,
        @NotBlank(message = "Image url is required")
        String url,
        String name
) {
}
