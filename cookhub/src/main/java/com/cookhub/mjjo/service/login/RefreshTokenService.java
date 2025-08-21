package com.cookhub.mjjo.service.login;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.cookhub.mjjo.jooq.generated.tables.ChRefreshToken.CH_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final DSLContext dsl;

    @Value("${jwt.refresh-exp-days:14}")
    private int refreshExpDays;

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
}
