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
    private Long id;

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
    @Column(nullable = false)
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User( String loginId, String password, String name, String email, String phone, LocalDate birthday, Gender gender, Role role,  LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
        this.gender = gender;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User user (String loginId, String password, String name, String email, String phone, LocalDate birthday, Gender gender, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(
                loginId,
                password,
                name,
                email,
                phone,
                birthday,
                gender,
                Role.USER,
                createdAt,
                updatedAt
        );
    }

    public static User admin (String loginId, String password, String name, String email, String phone, LocalDate birthday, Gender gender, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(
                loginId,
                password,
                name,
                email,
                phone,
                birthday,
                gender,
                Role.USER,
                createdAt,
                updatedAt
        );
    }

    public void updateInfo(String name, String email, String phone, LocalDate birthday) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
        this.updatedAt = LocalDateTime.now();
    }
}
