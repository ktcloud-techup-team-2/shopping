package com.kt.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailRequest {
    public record VerificationRequest(
            @NotBlank(message = "이메일을 입력해주세요.")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                    message = "유효한 이메일 주소를 입력해주세요.")
            String email
    ) {}

    public record VerificationConfirmRequest(
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message ="올바른 이메일 형식이 아닙니다.")
            String email,

            @NotBlank(message = "인증번호를 입력해주세요.")
            String code
    ) {}
}
