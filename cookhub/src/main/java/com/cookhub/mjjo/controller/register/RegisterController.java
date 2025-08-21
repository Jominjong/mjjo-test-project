package com.cookhub.mjjo.controller.register;

import com.cookhub.mjjo.dto.register.RegisterRequest;
import com.cookhub.mjjo.dto.register.RegisterResponse;
import com.cookhub.mjjo.service.register.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(registerService.register(request));
    }

    @Operation(summary = "회원 조회")
    @GetMapping("/{userNo}")
    public ResponseEntity<RegisterResponse> getUser(@PathVariable Integer userNo) {
        return ResponseEntity.ok(registerService.getUserById(userNo));
    }
}
