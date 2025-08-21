package com.cookhub.mjjo.dto.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @Email
    @NotBlank
    private String userEmail;

    @NotBlank
    private String userPw;

    @NotBlank
    private String userName;
}
