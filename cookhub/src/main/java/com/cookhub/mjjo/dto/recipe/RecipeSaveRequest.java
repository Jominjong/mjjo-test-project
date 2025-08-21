package com.cookhub.mjjo.dto.recipe;

import jakarta.validation.constraints.*;
import java.util.List;

public record RecipeSaveRequest(
        @NotNull Integer userNo,
        @NotBlank String title,
        String content,
        @NotNull Integer categoryNo,
        @Size(min = 0) List<IngredientDto> ingredients
) {
    public record IngredientDto(
            @NotBlank String name,
            @NotBlank String amount
    ) {}
}
