package com.malllite.catalog.dto;

public record UploadResponse(
        String url,
        String fileName,
        String originalName
) {
}
