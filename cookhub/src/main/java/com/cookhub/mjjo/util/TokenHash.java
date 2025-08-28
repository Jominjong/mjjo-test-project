package com.cookhub.mjjo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenHash {
    private static final SecureRandom RND = new SecureRandom();

    private TokenHash() {}

    /** URL-safe 랜덤 토큰 (평문) 생성 : URL-safe(패딩 없음) 256-bit 랜덤 토큰 생성 → 쿠키/URL에 안전하게 넣을 수 있음 */
    public static String newRawToken() {
        byte[] buf = new byte[32]; // 256-bit
        RND.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** DB에는 SHA-256 해시를 저장(평문 대신) : 토큰 평문은 클라이언트에만 주고, 서버 DB에는 SHA-256 해시만 저장하도록 도와줌 */
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
