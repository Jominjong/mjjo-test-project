package com.cookhub.mjjo.service.login;

import com.cookhub.mjjo.dto.login.LoginRequest;
import com.cookhub.mjjo.dto.login.LoginResponse;
import com.cookhub.mjjo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cookhub.mjjo.jooq.generated.tables.ChUsers.CH_USERS;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final DSLContext dsl;
    private final RefreshTokenService rtService;
    private final JwtUtil jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest req) {
        var rec = dsl.selectFrom(CH_USERS)
                .where(CH_USERS.USER_EMAIL.eq(req.getEmail()))
                .and(CH_USERS.DELETED_AT.isNull())
                .fetchOne();

        if (rec == null || !encoder.matches(req.getPassword(), rec.getUserPw())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        var roles = List.of("ROLE_USER");
        String access = jwt.issueAccess(rec.getUserNo(), rec.getUserEmail(), rec.getUserName(), roles);
        String refresh = rtService.issue(rec.getUserNo());

        return new LoginResponse(access, refresh, rec.getUserNo(), rec.getUserEmail(), rec.getUserName());
    }
}
