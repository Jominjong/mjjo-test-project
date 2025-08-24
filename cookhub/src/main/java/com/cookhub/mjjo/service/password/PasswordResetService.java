package com.cookhub.mjjo.service.password;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cookhub.mjjo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.cookhub.mjjo.jooq.generated.tables.ChUsers.CH_USERS;
import static com.cookhub.mjjo.jooq.generated.tables.ChRefreshToken.CH_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwt;

    /**
     * 비밀번호 리셋
     * @param resetToken  비밀번호 리셋용 JWT (claim: type=pwd_reset, email=...)
     * @param newPassword 새 비밀번호(평문)
     */
    @Transactional
    public void reset(String resetToken, String newPassword) {
        // 1) 입력값 검증 (400)
        if (resetToken == null || resetToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "resetToken is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newPassword is required");
        }
        // (선택) 간단한 정책 예시
        if (newPassword.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newPassword must be at least 4 characters");
        }

        // 2) 토큰 검증 (유효기간/서명 검증 실패 시 400)
        final DecodedJWT d;
        try {
            d = jwt.verify(resetToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token");
        }

        // 3) 리셋 전용 토큰 타입인지 확인
        if (!"pwd_reset".equals(d.getClaim("type").asString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a password-reset token");
        }

        // 4) 이메일 추출
        final String email = d.getClaim("email").asString();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token payload: email missing");
        }

        // 5) 비밀번호 해시 후 변경
        String hashed = passwordEncoder.encode(newPassword);
        int updated = dsl.update(CH_USERS)
                .set(CH_USERS.USER_PW, hashed)
                // ⚠️ jOOQ 생성된 컬럼명이 실제 스키마와 일치해야 합니다.
                // 만약 컬럼이 DELDTE_AT 로 생성되어 있다면 DELETED_AT 대신 그 컬럼을 사용하세요.
                .where(CH_USERS.USER_EMAIL.eq(email))
                .and(CH_USERS.DELETED_AT.isNull())
                .execute();

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // 6) 모든 Refresh Token 폐기 (강제 재로그인 유도)
        Integer userNo = dsl.select(CH_USERS.USER_NO)
                .from(CH_USERS)
                .where(CH_USERS.USER_EMAIL.eq(email))
                .and(CH_USERS.DELETED_AT.isNull())
                .fetchOne(CH_USERS.USER_NO);

        if (userNo != null) {
            dsl.deleteFrom(CH_REFRESH_TOKEN)
               .where(CH_REFRESH_TOKEN.USER_NO.eq(userNo))
               .execute();
        }
    }
}
