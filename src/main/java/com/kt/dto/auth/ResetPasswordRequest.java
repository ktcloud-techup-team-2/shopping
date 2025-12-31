package com.kt.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank
        String resetToken,
        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()\\+|=])[A-Za-z\\d~!@#$%^&*()\\+|=]{8,16}$",
                message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
        String newPassword,
        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()\\+|=])[A-Za-z\\d~!@#$%^&*()\\+|=]{8,16}$",
                message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
        String newPasswordConfirm
) {}
