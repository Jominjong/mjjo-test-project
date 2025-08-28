package com.cookhub.mjjo.controller;

import com.cookhub.mjjo.dto.auth.*;

import com.cookhub.mjjo.service.AuthService;

import com.cookhub.mjjo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.cookhub.mjjo.jooq.generated.tables.ChUsers.CH_USERS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final JwtUtil jwt;
    private final DSLContext dsl;
    private final AuthService authService;
    
    @Operation(summary = "[permit]로그인(토큰 발급)")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @Operation(summary = "[permit]토큰 갱신(Refresh → 새 Access/Refresh 발급, 회전)")
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody RefreshRequest req) {
    	//입력 검증
        if (req == null || req.getRefreshToken() == null || req.getRefreshToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "리프레쉬 토큰이 없습니다.");
        }
    	
        //토큰 유효 검증
        Integer userNo = authService.getUserNoIfValid(req.getRefreshToken());
        if (userNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }
        
        //이전 리프레쉬 토큰 즉시 만료(회전)
        authService.expire(req.getRefreshToken());
        
        var user = dsl.selectFrom(CH_USERS)
                .where(CH_USERS.USER_NO.eq(userNo))
                .fetchOne();
        
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        var roles = List.of("ROLE_USER");
        String newAccess = jwt.issueAccess(user.getUserNo(), user.getUserEmail(), user.getUserName(), roles);
        String newRefresh = authService.issue(user.getUserNo());

        return ResponseEntity.ok(new TokenPairResponse(newAccess, newRefresh));
    }

    @Operation(summary = "[permit]로그아웃(해당 Refresh 만료)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest req) {
    	if (req != null && req.getRefreshToken() != null && !req.getRefreshToken().isBlank()) {
            try { authService.expire(req.getRefreshToken()); } catch (Exception ignored) {}
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모든 기기에서 로그아웃(해당 사용자 모든 Refresh 만료)")
    @PostMapping("/logout-all/{userNo}")
    public ResponseEntity<Void> logoutAll(@PathVariable("userNo") Integer userNo) {
    	authService.expireAllByUser(userNo);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "토큰 검증 샘플(보호 없이 수동 체크)")
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
    	try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
            }
            var decoded = jwt.verify(authorization.substring(7));
            return ResponseEntity.ok(decoded.getClaims()); // userNo/email/name/roles 등
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    @Operation(summary = "[permit] 이메일 중복검사 (중복이 아닐 시 인증코드 발송)")
    @PostMapping("/register/check")
    public ResponseEntity<EmailCheckResponse> checkEmail(@Valid @RequestBody EmailCheckRequest request) {
        return ResponseEntity.ok(authService.checkEmailAndSendCode(request.email()));
    }

    @Operation(summary = "[permit] 회원가입 인증코드 검증 (성공 시 signupToken 발급)")
    @PostMapping("/register/verify")
    public ResponseEntity<VerifyCodeResponse> verifySignup(@Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verifySignupCode(request.email(), request.code()));
    }

    @Operation(summary = "[permit] 회원가입 완료")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
        @Parameter(description = "verify 단계에서 발급된 회원가입 토큰")
        @RequestHeader("X-Signup-Token") String signupToken,
        @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(signupToken, request));
    }
    
//    @Operation(summary = "[permit]회원가입 완료")
//    @PostMapping("/register")
//    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
//        return ResponseEntity.ok(authService.register(request));
//    }

    @Operation(summary = "회원 조회")
    @GetMapping("/register/{userNo}")
    public ResponseEntity<RegisterResponse> getUser(@PathVariable(name = "userNo", required = true) Integer userNo) {
        return ResponseEntity.ok(authService.getUserById(userNo));
    }
    
    @Operation(summary = "[permit]비밀번호 찾기 - 인증코드 발급")
    @PostMapping("/password/find")
    public ResponseEntity<Void> find(@Valid @RequestBody PasswordFindRequest req) {
    	authService.issueCode(req.email());
        // 보안상 존재 여부를 숨기기 위해 항상 200을 반환
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[permit]비밀번호 찾기 - 인증코드 검증(리셋 토큰 발급)")
    @PostMapping("/password/verify")
    public ResponseEntity<VerifyCodeResponse> verify(@Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(authService.verify(req.email(), req.code()));
    }

    @Operation(summary = "[permit]비밀번호 리셋 실행(유저 사용가능)")
    @PostMapping("/password/reset")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest req) {
    	authService.reset(req.resetToken(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
