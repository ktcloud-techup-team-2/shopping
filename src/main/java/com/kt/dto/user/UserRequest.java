package com.kt.dto.user;

import com.kt.domain.user.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class UserRequest {
    public record Create(
            @NotBlank(message = "아이디는 필수 입력값입니다.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$",
                    message = "아이디는 영문자와 숫자를 조합해야 합니다.")
            String loginId,

            @NotBlank(message = "비밀번호는 필수 입력값입니다.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()\\+|=])[A-Za-z\\d~!@#$%^&*()\\+|=]{8,16}$",
                    message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
            String password,

            @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
            String passwordConfirm,

            @NotBlank
            String name,

            @NotBlank
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            String email,

            @NotBlank
            @Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
            String phone,

            @NotNull
            Gender gender,

            @NotNull
            LocalDate birthday
    ) {}

    public record Update(
            @NotBlank
            String name,

            @NotBlank
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            String email,

            @NotBlank
            @Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
            String phone,

            LocalDate birthday
    ) {}

    public record PasswordChange(

            @NotBlank(message = "비밀번호는 필수 입력값입니다.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()\\+|=])[A-Za-z\\d~!@#$%^&*()\\+|=]{8,16}$",
                    message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
            String oldPassword,

            @NotBlank(message = "비밀번호는 필수 입력값입니다.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()\\+|=])[A-Za-z\\d~!@#$%^&*()\\+|=]{8,16}$",
                    message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다.")
            String newPassword,

            @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
            String newPasswordConfirm
    ) {}
}
