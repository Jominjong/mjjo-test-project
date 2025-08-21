package com.cookhub.mjjo.controller.recipe;

import com.cookhub.mjjo.dto.recipe.RecipeUpdateRequest;
import com.cookhub.mjjo.service.recipe.RecipeEditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeEditController {
	
	
    private final RecipeEditService recipeEditService;

    @PutMapping("/{boardNo}")
    public ResponseEntity<Void> update(@PathVariable("boardNo") @Min(1) Integer boardNo,
                                       @Valid @RequestBody RecipeUpdateRequest req) {
        recipeEditService.update(boardNo, req);
        return ResponseEntity.noContent().build();
    }
}
