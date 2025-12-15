package com.kt.security;

import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import java.util.Date;

@AllArgsConstructor
@ConfigurationProperties (prefix = "jwt")
public class JwtProperties {
    private final String secret;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;

    public Date getAccessTokenExpiration() {
        return new Date(new Date().getTime() + accessTokenExpiration);
    }

    public Date getRefreshTokenExpiration() {
        return new Date(new Date().getTime() + refreshTokenExpiration);
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
