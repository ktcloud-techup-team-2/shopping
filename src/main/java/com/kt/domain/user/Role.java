package com.kt.domain.user;

public enum Role {
    USER,
    ADMIN,
    SUPER_ADMIN;

    public String getKey() {
        return "ROLE_" + name();
    }
}
