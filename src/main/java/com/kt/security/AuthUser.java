package com.kt.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record AuthUser(
        Long id,
        Collection<? extends GrantedAuthority> authorities
) {
}
