package com.cookhub.mjjo.dto.recipe;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecipeUpdateRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Integer categoryNo,
        List<Ingredient> ingredients
) {
    // 중첩 타입 (record는 본질적으로 static 이라 사용에 용이)
    public static record Ingredient(
    		@NotBlank String name, 
    		@NotBlank String amount
    ) {}
}
