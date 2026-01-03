package com.kt.config.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kakao.auth")
public record KakaoAuthProperties(
        String client,
        String secret,
        String adminKey,
        String redirect
) {}
