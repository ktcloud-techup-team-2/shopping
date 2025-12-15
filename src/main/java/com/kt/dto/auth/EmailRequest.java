package com.kt.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "유효한 이메일 주소를 입력해주세요.")
        String email
) {}
