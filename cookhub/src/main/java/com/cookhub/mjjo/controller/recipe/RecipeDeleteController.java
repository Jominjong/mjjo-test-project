package com.cookhub.mjjo.controller.recipe;

import com.cookhub.mjjo.service.recipe.RecipeDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeDeleteController {

    private final RecipeDeleteService recipeDeleteService;

    @DeleteMapping("/{boardNo}")
    public ResponseEntity<Void> delete(@PathVariable Integer boardNo) {
        recipeDeleteService.delete(boardNo);
        return ResponseEntity.noContent().build();
    }
}
