package com.cookhub.mjjo.dto.recipe;

import jakarta.validation.constraints.*;
import java.util.List;

public record RecipeUpdateRequest(
        @NotBlank String title,
        String content,
        @NotNull Integer categoryNo,
        @Size(min = 0) List<RecipeSaveRequest.IngredientDto> ingredients
) {}
