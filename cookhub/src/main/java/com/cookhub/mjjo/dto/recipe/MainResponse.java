package com.cookhub.mjjo.dto.recipe;

import java.time.LocalDateTime;
import java.util.List;

public record MainResponse(
        List<Item> items,
        int page,     // 1-base
        int size,
        long total,
        int totalPages,
        boolean hasNext
) {
    public record Item(
            Integer boardNo,
            String title,
            Integer categoryNo,
            Integer userNo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
