package com.cookhub.mjjo.dto.recipe;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeResponse(
        Integer boardNo,
        Integer userNo,
        String title,
        String content,
        Integer categoryNo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<Ingredient> ingredients
) {
    public record Ingredient(
            Integer ingNo,
            String name,
            String amount
    ) {}
}
