package com.kt.dto.auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest (
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$",
                message = "아이디는 영문자와 숫자를 조합해야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()\\+|=])[A-Za-z\\d~!@#$%^&*()\\+|=]{8,16}$",
                message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
        String password
) {
}
