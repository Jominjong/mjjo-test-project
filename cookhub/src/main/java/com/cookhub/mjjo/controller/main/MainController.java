package com.cookhub.mjjo.controller.main;

import com.cookhub.mjjo.dto.main.MainResponse;
import com.cookhub.mjjo.service.main.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class MainController {

    private final MainService mainService;

    @GetMapping
    public MainResponse list(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Integer categoryNo,
                             @RequestParam(required = false) String keyword) {
        return mainService.list(page, size, categoryNo, keyword);
    }
}
