package com.malllite.common.storage;

public record StoredFile(
        String path,
        String key,
        String originalName
) {
}
