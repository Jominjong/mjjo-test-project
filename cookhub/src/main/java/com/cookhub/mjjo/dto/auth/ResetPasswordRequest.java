package com.cookhub.mjjo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank String resetToken,
    @NotBlank @Size(min = 4, max = 64) String newPassword
) {}