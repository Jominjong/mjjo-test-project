package com.cookhub.mjjo.dto.login;
import lombok.AllArgsConstructor; import lombok.Getter;
@Getter @AllArgsConstructor
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
}