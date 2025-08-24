package com.cookhub.mjjo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private Integer userNo;
    private String email;
    private String name;
}
