package com.kt.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @NotNull
    private String name;
    private String loginId;
    private String password;
    private String email;
    private String phone;
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(String name, String loginId, String password, String email, String phone, LocalDate birthday, Gender gender, Role role,  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
        this.gender = gender;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateInfo (String name, String email, String phone, LocalDate birthday, LocalDateTime updatedAt) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
        this.updatedAt = updatedAt;
    }
}
