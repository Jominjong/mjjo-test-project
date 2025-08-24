package com.cookhub.mjjo.controller;

import com.cookhub.mjjo.security.AuthUser;
import com.cookhub.mjjo.service.RecipeService;
import com.cookhub.mjjo.dto.recipe.*;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    
	// GET /api/recipes?page=1&size=10&categoryNo=...&keyword=...
    @Operation(summary = "레시피 전체 조회")
	@GetMapping
	public MainResponse list(@RequestParam(name = "page", defaultValue = "1") int page,
							 @RequestParam(name = "size", defaultValue = "10") int size,
							 @RequestParam(name = "categoryNo", required = false) Integer categoryNo,
							 @RequestParam(name = "keyword", required = false) String keyword,
							 @AuthenticationPrincipal AuthUser me){
    	if (me == null) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED);
    	
		return recipeService.list(page, size, categoryNo, keyword, me.userNo());
	}
    
    @Operation(summary = "레시피 상세 조회")
    @GetMapping("/{boardNo}")
    public RecipeResponse get(@PathVariable(name = "boardNo", required = true) Integer boardNo,
    						  @AuthenticationPrincipal AuthUser me) {
    	if (me == null) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED);
    	
        return recipeService.getByBoardNo(me.userNo(), boardNo);
    }
	
	@Operation(summary = "레시피 추가")
	@PostMapping
	public ResponseEntity<Integer> create(@Valid @RequestBody RecipeSaveRequest req,
										  @AuthenticationPrincipal AuthUser me) {
		if (me == null) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED);
		
		Integer boardNo = recipeService.create(me.userNo(), req);
		return ResponseEntity.ok(boardNo);
	}
	
	@Operation(summary = "레시피 수정")
    @PutMapping("/{boardNo}")
    public ResponseEntity<Void> update(@PathVariable("boardNo") @Min(1) Integer boardNo,
                                       @Valid @RequestBody RecipeUpdateRequest req,
                                       @AuthenticationPrincipal AuthUser me) {
		if (me == null) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED);
		
		recipeService.update(me.userNo(), boardNo, req);
        return ResponseEntity.noContent().build();
    }
	
	@Operation(summary = "레시피 삭제")
	@DeleteMapping("/{boardNo}")
	public ResponseEntity<Void> delete(@PathVariable("boardNo") Integer boardNo,
									   @AuthenticationPrincipal AuthUser me) {
		if (me == null) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED);
		recipeService.delete(me.userNo(), boardNo);

	    return ResponseEntity.noContent().build();
	}
}
