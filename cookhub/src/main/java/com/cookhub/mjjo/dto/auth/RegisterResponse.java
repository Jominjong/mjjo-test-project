package com.cookhub.mjjo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    @NotBlank private Integer userNo;
    @NotBlank private String email;
    @NotBlank private String name;
}
