package com.cookhub.mjjo.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Integer userNo;
    private String email;
    private String name;
}
