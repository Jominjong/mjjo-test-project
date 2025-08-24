package com.cookhub.mjjo.controller.recipe;

import com.cookhub.mjjo.dto.recipe.RecipeResponse;
import com.cookhub.mjjo.security.AuthUser;
import com.cookhub.mjjo.service.recipe.RecipeViewService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeViewControllerLegacy {

    private final RecipeViewService recipeViewService;

    @Operation(summary = "레시피 상세 조회")
    @GetMapping("/{boardNo}")
    public RecipeResponse get(@PathVariable(name = "boardNo", required = true) Integer boardNo,
    						  @AuthenticationPrincipal AuthUser me) {
        return recipeViewService.getByBoardNo(me.userNo(), boardNo);
    }
}
