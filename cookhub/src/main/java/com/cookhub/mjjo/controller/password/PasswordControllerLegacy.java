package com.cookhub.mjjo.controller.password;

import com.cookhub.mjjo.dto.password.*;
import com.cookhub.mjjo.service.password.PasswordFindService;
import com.cookhub.mjjo.service.password.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password")
public class PasswordControllerLegacy {

    private final PasswordFindService  passwordFindService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 찾기 - 인증코드 발급")
    @PostMapping("/find")
    public ResponseEntity<Void> find(@Valid @RequestBody PasswordFindRequest req) {
        passwordFindService.issueCode(req.email());
        // 보안상 존재 여부를 숨기기 위해 항상 200을 반환
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 검증(리셋 토큰 발급)")
    @PostMapping("/verify")
    public ResponseEntity<VerifyCodeResponse> verify(@Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(passwordFindService.verify(req.email(), req.code()));
    }

    @Operation(summary = "비밀번호 리셋 실행")
    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.reset(req.resetToken(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
