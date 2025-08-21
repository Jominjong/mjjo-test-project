package com.cookhub.mjjo.controller.recipe;

import com.cookhub.mjjo.dto.recipe.RecipeSaveRequest;
import com.cookhub.mjjo.service.recipe.RecipeAddService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeAddController {

    private final RecipeAddService recipeAddService;

    @PostMapping
    public ResponseEntity<Integer> create(@Valid @RequestBody RecipeSaveRequest req) {
        Integer boardNo = recipeAddService.create(req);
        return ResponseEntity.ok(boardNo);
    }
}
