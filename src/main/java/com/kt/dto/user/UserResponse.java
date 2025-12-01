package com.kt.dto.user;

import com.kt.domain.user.Gender;
import com.kt.domain.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String loginId,
        String name,
        String email,
        String phone,
        Gender gender,
        LocalDate birthday
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                user.getBirthday()
        );
    }
}