package com.cookhub.mjjo.controller.login;

import com.cookhub.mjjo.dto.login.LoginRequest;
import com.cookhub.mjjo.dto.login.LoginResponse;
import com.cookhub.mjjo.service.login.LoginService;
import com.cookhub.mjjo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginControllerLegacy {

    private final LoginService loginService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "로그인(토큰 발급)")
    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @Operation(summary = "토큰 검증 샘플(보호 없이 수동 체크)")
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authorization) {
        // Authorization: Bearer <token>
        String token = authorization.replace("Bearer ", "");
        var jwt = jwtUtil.verify(token);
        return ResponseEntity.ok(jwt.getClaims()); // userNo/email/name 등 확인 가능
    }
}
