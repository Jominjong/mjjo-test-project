package com.cookhub.mjjo.controller;

import com.cookhub.mjjo.security.AuthUser;

import com.cookhub.mjjo.dto.main.MainResponse;
import com.cookhub.mjjo.service.main.MainService;

import com.cookhub.mjjo.dto.recipe.RecipeSaveRequest;
import com.cookhub.mjjo.service.recipe.RecipeAddService;

import com.cookhub.mjjo.service.recipe.RecipeDeleteService;

import com.cookhub.mjjo.dto.recipe.RecipeUpdateRequest;
import com.cookhub.mjjo.service.recipe.RecipeEditService;

import com.cookhub.mjjo.dto.recipe.RecipeResponse;
import com.cookhub.mjjo.service.recipe.RecipeViewService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/recipes")
public class RecipeController {

    private final MainService mainService;
    private final RecipeAddService recipeAddService;
    private final RecipeDeleteService recipeDeleteService;
    private final RecipeEditService recipeEditService;
    private final RecipeViewService recipeViewService;
    
	// GET /api/recipes?page=1&size=10&categoryNo=...&keyword=...
    @Operation(summary = "레시피 전체 조회")
	@GetMapping
	public MainResponse list(@RequestParam(name = "page", defaultValue = "1") int page,
							 @RequestParam(name = "size", defaultValue = "10") int size,
							 @RequestParam(name = "categoryNo", required = false) Integer categoryNo,
							 @RequestParam(name = "keyword", required = false) String keyword,
							 @AuthenticationPrincipal AuthUser me){
		return mainService.list(page, size, categoryNo, keyword, me.userNo());
	}
    
    @Operation(summary = "레시피 상세 조회")
    @GetMapping("/{boardNo}")
    public RecipeResponse get(@PathVariable(name = "boardNo", required = true) Integer boardNo,
    						  @AuthenticationPrincipal AuthUser me) {
        return recipeViewService.getByBoardNo(me.userNo(), boardNo);
    }
	
	@Operation(summary = "레시피 추가")
	@PostMapping
	public ResponseEntity<Integer> create(@Valid @RequestBody RecipeSaveRequest req) {
		Integer boardNo = recipeAddService.create(req);
		return ResponseEntity.ok(boardNo);
	}
	
	@Operation(summary = "레시피 수정")
    @PutMapping("/{boardNo}")
    public ResponseEntity<Void> update(@PathVariable("boardNo") @Min(1) Integer boardNo,
                                       @Valid @RequestBody RecipeUpdateRequest req) {
        recipeEditService.update(boardNo, req);
        return ResponseEntity.noContent().build();
    }
	
	@Operation(summary = "레시피 삭제")
	@DeleteMapping("/{boardNo}")
	public ResponseEntity<Void> delete(@PathVariable("boardNo") Integer boardNo) {
	    recipeDeleteService.delete(boardNo);
	    return ResponseEntity.noContent().build();
	}
}
