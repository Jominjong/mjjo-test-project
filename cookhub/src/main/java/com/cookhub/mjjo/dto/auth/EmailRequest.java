package com.cookhub.mjjo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailRequest {

    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 300, message = "이메일은 300자 이하여야 합니다.")
    private String email;

}