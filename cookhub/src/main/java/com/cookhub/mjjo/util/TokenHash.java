package com.cookhub.mjjo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenHash {
    private static final SecureRandom RND = new SecureRandom();

    private TokenHash() {}

    /** URL-safe 랜덤 토큰 (평문) 생성 */
    public static String newRawToken() {
        byte[] buf = new byte[32]; // 256-bit
        RND.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** DB에는 SHA-256 해시를 저장(평문 대신) */
    public static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(dig);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
