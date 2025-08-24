package com.cookhub.mjjo.dto.register;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @Email private  String email;
    private String password;
    private String name;
}

