package com.kt.dto.user;

import com.kt.domain.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UserSignUpRequest (
        @Schema(description = "로그인 ID", example = "test1234")
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$",
                message = "아이디는 영문자와 숫자를 조합해야 합니다.")
        String loginId,

        @Schema(description = "비밀번호", example = "Test1234!")
        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()\\+|=])[A-Za-z\\d~!@#$%^&*()\\+|=]{8,16}$",
                message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
        String password,

        @Schema(description = "비밀번호 확인", example = "Test1234!")
        @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
        String passwordConfirm,
        @NotBlank
        @Schema(description = "이름", example = "홍길동")
        String name,
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        @Schema(description = "이메일", example = "example123@gmail.com")
        String email,
        @NotBlank
        @Schema(description = "전화번호", example = "010-1234-4567")
        @Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
        String phone,
        @NotNull
        @Schema(example = "MALE")
        Gender gender,
        @NotNull
        @Schema(example = "1999-09-09")
        LocalDate birthday
) {
}
