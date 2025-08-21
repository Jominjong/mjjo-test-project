package com.cookhub.mjjo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {

    private final Algorithm algorithm;
    private final String issuer;
    private final long expMinutes;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.exp-minutes}") long expMinutes
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expMinutes = expMinutes;
    }

    public String issue(Integer userNo, String email, String name) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(expMinutes, ChronoUnit.MINUTES)))
                .withClaim("userNo", userNo)
                .withClaim("email", email)
                .withClaim("name", name)
                .sign(algorithm);
    }

    public com.auth0.jwt.interfaces.DecodedJWT verify(String token) {
        return JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
    }
}
