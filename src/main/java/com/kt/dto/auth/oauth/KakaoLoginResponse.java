package com.kt.dto.auth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoLoginResponse {

    public record OAuthToken (
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Integer expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("refresh_token_expires_in") Integer refreshTokenExpiresIn,
            String scope
    ) {}

    public record KakaoUserInfo (
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount,
            Properties properties
    ) {
        public String emailOrNull() {
            return kakaoAccount != null ? kakaoAccount.email() :  null;
        }

        public String nicknameOrNull() {
            return properties != null ? properties.nickname : null;
        }
    }

    public record KakaoAccount(
            String email,
            @JsonProperty("has_email") Boolean hasEmail
    ) {}

    public record Properties(
            String nickname
    ) {}
}
