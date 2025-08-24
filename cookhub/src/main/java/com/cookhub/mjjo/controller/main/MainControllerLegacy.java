package com.cookhub.mjjo.controller.main;

import com.cookhub.mjjo.dto.main.MainResponse;
import com.cookhub.mjjo.security.AuthUser;
import com.cookhub.mjjo.service.main.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class MainControllerLegacy {

    private final MainService mainService;
    
	 // GET /api/recipes?page=1&size=10&categoryNo=...&keyword=...
	 @GetMapping
	 public MainResponse list(@RequestParam(name = "page", defaultValue = "1") int page,
	                          @RequestParam(name = "size", defaultValue = "10") int size,
	                          @RequestParam(name = "categoryNo", required = false) Integer categoryNo,
	                          @RequestParam(name = "keyword", required = false) String keyword,
	                          @AuthenticationPrincipal AuthUser me){
	     return mainService.list(page, size, categoryNo, keyword, me.userNo());
    }
}
