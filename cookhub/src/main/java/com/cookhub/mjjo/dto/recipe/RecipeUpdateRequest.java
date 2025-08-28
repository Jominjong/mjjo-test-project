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
    public static record Ingredient(
    		@NotBlank String name, 
    		@NotBlank String amount
    ) {}
}
