package com.kt.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FindPasswordRequest(
        @NotBlank String loginId,
        @Email @NotBlank String email,
        @NotBlank String name
) {}
