package com.cookhub.mjjo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final Algorithm algorithm;
    private final String issuer;
    private final long accessExpMinutes;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer:cookhub}") String issuer,
            @Value("${jwt.access-exp-minutes:30}") long accessExpMinutes
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.accessExpMinutes = accessExpMinutes;
    }

    public String issueAccess(Integer userNo, String email, String name, List<String> roles) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(accessExpMinutes, ChronoUnit.MINUTES)))
                .withClaim("type", "access")
                .withClaim("userNo", userNo)
                .withClaim("email", email)
                .withClaim("name", name)
                .withClaim("roles", roles)
                .sign(algorithm);
    }

    public com.auth0.jwt.interfaces.DecodedJWT verify(String token) {
        return JWT.require(algorithm).withIssuer(issuer).build().verify(token);
    }
    
 // JwtUtil.java 내부 예시
    public String createToken(String type, String email, Duration ttl) {
        Instant now = Instant.now();
        return JWT.create()
        	.withIssuer(issuer)
        	.withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plus(ttl)))
            .withClaim("type", type)   // "pwd_reset" 등
            .withClaim("email", email)
            .sign(algorithm);   // 기존 서명 알고리즘/시크릿 재사용
    }

}
