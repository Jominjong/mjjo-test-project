package com.cookhub.mjjo.dto.auth;

public record VerifyCodeResponse(
    String resetToken,
    long   expiresInSec
) {}