package com.cookhub.mjjo.service;

import com.cookhub.mjjo.util.EmailUtil;
import com.cookhub.mjjo.util.JwtUtil;

import jakarta.mail.MessagingException;

import com.cookhub.mjjo.dto.auth.*;

import java.util.List;
import java.time.Duration;
import org.jooq.DSLContext;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.cookhub.mjjo.jooq.generated.tables.ChUsers.CH_USERS;
import static com.cookhub.mjjo.jooq.generated.tables.ChRefreshToken.CH_REFRESH_TOKEN;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final DSLContext dsl;
    private final JwtUtil jwt;
    private final PasswordEncoder encoder;
    private final EmailUtil emailUtil;
    
    @Value("${jwt.refresh-exp-days:14}")
    private int refreshExpDays;
    
    private final StringRedisTemplate redis;
    
    private static final Duration CODE_TTL   = Duration.ofMinutes(10);
    private static final Duration RESET_TTL  = Duration.ofMinutes(10);

    private String codeKey(String email) {
        return "pwd:code:" + email.toLowerCase();
    }
    
    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
    
/*--------------------------------------------------------------------------------------------*/
/*login*/
/*--------------------------------------------------------------------------------------------*/
    /*로그인*/
    @Transactional
    public LoginResponse login(LoginRequest req) {
    	final String email = normalize(req.getEmail());
    	
        var rec = dsl.selectFrom(CH_USERS)
                .where(CH_USERS.USER_EMAIL.eq(email))
                .and(CH_USERS.DELETED_AT.isNull())
                .fetchOne();

        if (rec == null || !encoder.matches(req.getPassword(), rec.getUserPw())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        var roles = List.of("ROLE_USER");
        String access = jwt.issueAccess(rec.getUserNo(), rec.getUserEmail(), rec.getUserName(), roles);
        String refresh = issue(rec.getUserNo());

        return new LoginResponse(access, refresh, rec.getUserNo(), rec.getUserEmail(), rec.getUserName());
    }
    
    /** 새 RT 저장 (raw는 반환용, DB에는 해시 저장) */
    public String issue(Integer userNo) {
        String raw = com.cookhub.mjjo.util.TokenHash.newRawToken();
        String hash = com.cookhub.mjjo.util.TokenHash.sha256(raw);

        dsl.insertInto(CH_REFRESH_TOKEN)
           .set(CH_REFRESH_TOKEN.USER_NO, userNo)
           .set(CH_REFRESH_TOKEN.REFRESH_TOKEN, hash)
           .set(CH_REFRESH_TOKEN.ISSUED_AT, LocalDateTime.now())
           .set(CH_REFRESH_TOKEN.EXPIRED_AT, LocalDateTime.now().plusDays(refreshExpDays))
           .execute();
        return raw;
    }

    public Integer getUserNoIfValid(String raw) {
        String hash = com.cookhub.mjjo.util.TokenHash.sha256(raw);
        var rec = dsl.selectFrom(CH_REFRESH_TOKEN)
                .where(CH_REFRESH_TOKEN.REFRESH_TOKEN.eq(hash))
                .and(CH_REFRESH_TOKEN.EXPIRED_AT.isNull().or(CH_REFRESH_TOKEN.EXPIRED_AT.gt(LocalDateTime.now())))
                .fetchOne();
        return rec == null ? null : rec.getUserNo();
    }

    /** 사용된 RT 즉시 만료(회전 전용) */
    public void expire(String raw) {
        String hash = com.cookhub.mjjo.util.TokenHash.sha256(raw);
        dsl.update(CH_REFRESH_TOKEN)
           .set(CH_REFRESH_TOKEN.EXPIRED_AT, LocalDateTime.now())
           .where(CH_REFRESH_TOKEN.REFRESH_TOKEN.eq(hash))
           .and(CH_REFRESH_TOKEN.EXPIRED_AT.isNull().or(CH_REFRESH_TOKEN.EXPIRED_AT.gt(LocalDateTime.now())))
           .execute();
    }

    /** 해당 유저의 모든 유효 RT 만료(로그아웃-올) */
    public void expireAllByUser(Integer userNo) {
        dsl.update(CH_REFRESH_TOKEN)
           .set(CH_REFRESH_TOKEN.EXPIRED_AT, LocalDateTime.now())
           .where(CH_REFRESH_TOKEN.USER_NO.eq(userNo))
           .and(CH_REFRESH_TOKEN.EXPIRED_AT.isNull().or(CH_REFRESH_TOKEN.EXPIRED_AT.gt(LocalDateTime.now())))
           .execute();
    }

/*--------------------------------------------------------------------------------------------*/
/*register*/
/*--------------------------------------------------------------------------------------------*/    
    @Transactional
    public RegisterResponse register(RegisterRequest req) {
    	
    	final String email = normalize(req.getEmail());
    	
        boolean exists = dsl.fetchExists(
                dsl.selectFrom(CH_USERS).where(CH_USERS.USER_EMAIL.eq(req.getEmail()))
        );

        if (exists) {
        	throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다.");
        }

        var rec = dsl.insertInto(CH_USERS)
                .set(CH_USERS.USER_EMAIL, email)
                .set(CH_USERS.USER_PW, encoder.encode(req.getPassword()))
                .set(CH_USERS.USER_NAME, req.getName())
                .returning(CH_USERS.USER_NO, CH_USERS.USER_EMAIL, CH_USERS.USER_NAME)
                .fetchOne();

        return new RegisterResponse(
                rec.get(CH_USERS.USER_NO),
                rec.get(CH_USERS.USER_EMAIL),
                rec.get(CH_USERS.USER_NAME)
        );
    }

    public RegisterResponse getUserById(Integer userNo) {
        var rec = dsl.selectFrom(CH_USERS)
                .where(CH_USERS.USER_NO.eq(userNo))
                .fetchOne();

        if (rec == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return new RegisterResponse(
                rec.get(CH_USERS.USER_NO),
                rec.get(CH_USERS.USER_EMAIL),
                rec.get(CH_USERS.USER_NAME)
        );
    }

/*--------------------------------------------------------------------------------------------*/
/*password-find*/
/*--------------------------------------------------------------------------------------------*/  
    public void issueCode(String rawEmail) {
    	final String email = normalize(rawEmail);
    	
        // 유저 존재 여부 확인 (있든 없든 200 반환: 사용자 열람 방지)
        boolean exists = dsl.fetchExists(
            dsl.selectOne().from(CH_USERS)
               .where(CH_USERS.USER_EMAIL.eq(email))
               .and(CH_USERS.DELETED_AT.isNull())
        );

        
        if (exists) {
        	// 6자리 코드 생성
            String code = generate6DigitCode();
            
            // 존재할 때만 저장 — 혹은 항상 저장해도 무방
        	redis.opsForValue().set(codeKey(email), code, CODE_TTL);

            // ✅ 제목/본문을 “비밀번호 재설정”으로 명확히
            String subject = "[CookHub] 비밀번호 재설정 인증코드";
            String html = """
                <div style="font-family:system-ui,Arial,sans-serif;line-height:1.6">
                  <h2>비밀번호 재설정 인증코드</h2>
                  <p>아래 6자리 코드를 입력해 주세요.</p>
                  <div style="font-size:24px;font-weight:700;letter-spacing:4px; 
                  padding:12px 16px;border:1px solid #ddd;display:inline-block"> %s </div>
                  <p style="color:#666">유효시간: %d분</p>
                </div>"""
            .formatted(code, CODE_TTL.toMinutes());

            try {
                emailUtil.sendEmail(email, subject, html);  // ✅ 존재할 때만 발송
                log.debug("인증 코드 이메일 발송 완료 - email: {}", email);
            } catch (MessagingException e) {
                log.error("인증 코드 이메일 발송 실패 - email: {}, error: {}", email, e.getMessage());
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다.");
            }

            // (선택) 개발 환경에서만 로그로 코드 보여주기
            // if (isDevProfile) log.info("[DEV] {} => code={}", email, code);
        }
        
        // 실제로는 이메일 발송 필요. 지금은 개발 편의로 로그 출력.
        //System.out.println("[PasswordFind] email=" + email + ", code=" + code + " (TTL " + CODE_TTL.toMinutes() + "m)");
    }

    public VerifyCodeResponse verify(String rawEmail, String code) {
    	
    	final String email = normalize(rawEmail);
    	
        String key  = codeKey(email);
        String real = redis.opsForValue().get(key);
        if (real == null || !real.equals(code)) {
        	//400으로 명확히
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "인증코드가 유효하지 않거나 만료되었습니다.");
        }
        // 일회성 사용: 코드 제거
        redis.delete(key);

        // 비밀번호 리셋 전용 토큰(JWT) 발급 (Authorization 헤더로 쓰지 않고 바디로만 사용)
        // claim: type=pwd_reset, email=...
        String resetToken = jwt.createToken("pwd_reset", email, RESET_TTL);
        return new VerifyCodeResponse(resetToken, RESET_TTL.toSeconds());
    }

    private String generate6DigitCode() {
        SecureRandom rnd = new SecureRandom();
        int v = rnd.nextInt(1_000_000);
        
        return String.format("%06d", v);
    }
    
    /*비밀번호 리셋*/
    @Transactional
    public void reset(String resetToken, String newPassword) {
        // 1) 입력값 검증 (400)
        if (resetToken == null || resetToken.isBlank()) {
        	 throw new org.springframework.web.server.ResponseStatusException(
        	            org.springframework.http.HttpStatus.BAD_REQUEST, "resetToken is required");
        }
        
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 4) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "newPassword must be at least 4 characters");
        }

        // 2) 토큰 검증 (유효기간/서명 검증 실패 시 400)
        final DecodedJWT d;
        try {
            d = jwt.verify(resetToken);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired reset token");
        }

        // 3) 리셋 전용 토큰 타입인지 확인
        if (!"pwd_reset".equals(d.getClaim("type").asString())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Not a password-reset token");
        }

        // 4) 이메일 추출
        final String email = normalize(d.getClaim("email").asString());
        if (email.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid token payload: email missing");
        }

        // 5) 비밀번호 해시 후 변경
        String hashed = encoder.encode(newPassword);
        int updated = dsl.update(CH_USERS)
                .set(CH_USERS.USER_PW, hashed)
                // ⚠️ jOOQ 생성된 컬럼명이 실제 스키마와 일치해야 합니다.
                // 만약 컬럼이 DELDTE_AT 로 생성되어 있다면 DELETED_AT 대신 그 컬럼을 사용하세요.
                .where(CH_USERS.USER_EMAIL.eq(email))
                .and(CH_USERS.DELETED_AT.isNull())
                .execute();

        if (updated == 0) {
        	throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "User not found");
        }

        // 6) 모든 Refresh Token 폐기
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
