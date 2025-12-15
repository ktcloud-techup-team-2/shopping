package com.kt.domain.user;

public enum Role {
    USER,
    ADMIN;

    public String getKey() {
        return "ROLE_" + name();
    }
}
