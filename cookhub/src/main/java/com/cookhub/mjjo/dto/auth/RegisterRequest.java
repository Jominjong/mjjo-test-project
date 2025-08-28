package com.cookhub.mjjo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @Email private  String email;
    @NotBlank private String password;
    @NotBlank private String name;
}

