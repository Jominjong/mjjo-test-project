package com.cookhub.mjjo.dto.auth;

public record EmailCheckResponse(
    String email,
    boolean available,          // 사용 가능 여부
    boolean codeSent,           // 인증코드 발송했는지
    long codeTtlSeconds,        // 코드 유효시간(초) — 발송 안 했으면 0
    long cooldownSeconds        // 재발송 쿨다운 남은 시간(초) — 없으면 0
) {}