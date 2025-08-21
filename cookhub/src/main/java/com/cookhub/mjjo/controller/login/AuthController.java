package com.cookhub.mjjo.controller.login;

import com.cookhub.mjjo.dto.login.RefreshRequest;
import com.cookhub.mjjo.dto.login.TokenPairResponse;
import com.cookhub.mjjo.service.login.RefreshTokenService;
import com.cookhub.mjjo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cookhub.mjjo.jooq.generated.tables.ChUsers.CH_USERS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenService rtService;
    private final JwtUtil jwt;
    private final DSLContext dsl;

    @Operation(summary = "토큰 갱신(Refresh → 새 Access/Refresh 발급, 회전)")
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody RefreshRequest req) {
        Integer userNo = rtService.getUserNoIfValid(req.getRefreshToken());
        if (userNo == null) throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");

        // 이전 RT 즉시 만료(회전)
        rtService.expire(req.getRefreshToken());

        var user = dsl.selectFrom(CH_USERS)
                .where(CH_USERS.USER_NO.eq(userNo))
                .fetchOne();
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        var roles = List.of("ROLE_USER");
        String newAccess = jwt.issueAccess(user.getUserNo(), user.getUserEmail(), user.getUserName(), roles);
        String newRefresh = rtService.issue(user.getUserNo());

        return ResponseEntity.ok(new TokenPairResponse(newAccess, newRefresh));
    }

    @Operation(summary = "로그아웃(해당 Refresh 만료)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest req) {
        rtService.expire(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모든 기기에서 로그아웃(해당 사용자 모든 Refresh 만료)")
    @PostMapping("/logout-all/{userNo}")
    public ResponseEntity<Void> logoutAll(@PathVariable("userNo") Integer userNo) {
        rtService.expireAllByUser(userNo);
        return ResponseEntity.noContent().build();
    }
}
