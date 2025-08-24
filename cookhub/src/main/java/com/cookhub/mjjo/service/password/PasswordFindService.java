package com.cookhub.mjjo.service.password;

import com.cookhub.mjjo.dto.password.VerifyCodeResponse;
import com.cookhub.mjjo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

import static com.cookhub.mjjo.jooq.generated.tables.ChUsers.CH_USERS;

@Service
@RequiredArgsConstructor
public class PasswordFindService {

    private final DSLContext dsl;
    private final StringRedisTemplate redis;
    private final JwtUtil jwt;

    private static final Duration CODE_TTL   = Duration.ofMinutes(10);
    private static final Duration RESET_TTL  = Duration.ofMinutes(10);

    private String codeKey(String email) {
        return "pwd:code:" + email.toLowerCase();
    }

    public void issueCode(String email) {
        // 유저 존재 여부 확인 (있든 없든 200 반환: 사용자 열람 방지)
        boolean exists = dsl.fetchExists(
            dsl.selectOne().from(CH_USERS)
               .where(CH_USERS.USER_EMAIL.eq(email))
               .and(CH_USERS.DELETED_AT.isNull())
        );

        // 6자리 코드 생성
        String code = generate6DigitCode();
        // 존재할 때만 저장(선택) — 혹은 항상 저장해도 무방
        if (exists) {
            redis.opsForValue().set(codeKey(email), code, CODE_TTL);
        }

        // 실제로는 이메일 발송 필요. 지금은 개발 편의로 로그 출력.
        System.out.println("[PasswordFind] email=" + email + ", code=" + code + " (TTL " + CODE_TTL.toMinutes() + "m)");
    }

    public VerifyCodeResponse verify(String email, String code) {
        String key  = codeKey(email);
        String real = redis.opsForValue().get(key);
        if (real == null || !real.equals(code)) {
            throw new IllegalArgumentException("인증코드가 유효하지 않거나 만료되었습니다.");
        }
        // 일회성 사용: 코드 제거
        redis.delete(key);

        // 비밀번호 리셋 전용 토큰(JWT) 발급 (Authorization 헤더로 쓰지 않고 바디로만 사용)
        // claim: type=pwd_reset, email=...
        String resetToken = jwt.createToken(
            "pwd_reset",
            email,
            RESET_TTL
        );

        return new VerifyCodeResponse(resetToken, RESET_TTL.toSeconds());
    }

    private String generate6DigitCode() {
        SecureRandom rnd = new SecureRandom();
        int v = rnd.nextInt(1_000_000);
        return String.format("%06d", v);
    }
}
