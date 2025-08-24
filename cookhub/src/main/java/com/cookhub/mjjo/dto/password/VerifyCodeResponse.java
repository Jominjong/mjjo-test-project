package com.cookhub.mjjo.dto.password;

public record VerifyCodeResponse(
    String resetToken,
    long   expiresInSec
) {}