package com.cookhub.mjjo.dto.auth;
import lombok.AllArgsConstructor; import lombok.Getter;
@Getter @AllArgsConstructor
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
}