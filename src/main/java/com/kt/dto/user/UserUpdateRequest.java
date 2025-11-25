package com.kt.dto.user;

import com.kt.domain.user.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UserUpdateRequest (
        @NotBlank
        String name,
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        String email,
        @NotBlank
        @Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
        String phone,
        LocalDate birthday
) {
}
