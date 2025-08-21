package com.cookhub.mjjo.controller.recipe;

import com.cookhub.mjjo.dto.recipe.RecipeUpdateRequest;
import com.cookhub.mjjo.service.recipe.RecipeEditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeEditController {

    private final RecipeEditService recipeEditService;

    @PutMapping("/{boardNo}")
    public ResponseEntity<Void> update(@PathVariable Integer boardNo,
                                       @Valid @RequestBody RecipeUpdateRequest req) {
        recipeEditService.update(boardNo, req);
        return ResponseEntity.noContent().build();
    }
}
