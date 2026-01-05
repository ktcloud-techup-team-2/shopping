package com.kt.common.oauth;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.config.oauth.KakaoAuthProperties;
import com.kt.dto.auth.oauth.KakaoLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoUtil {
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final RestTemplate restTemplate;
    private final KakaoAuthProperties kakaoAuthProperties;

    public KakaoLoginResponse.OAuthToken requestToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoAuthProperties.client());
        params.add("client_secret", kakaoAuthProperties.secret());
        params.add("redirect_uri", kakaoAuthProperties.redirect());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoLoginResponse.OAuthToken> response = restTemplate.exchange(
                    TOKEN_URL, HttpMethod.POST, request, KakaoLoginResponse.OAuthToken.class);
            KakaoLoginResponse.OAuthToken body = response.getBody();
            if(body == null) {
                throw new CustomException(ErrorCode.KAKAO_TOKEN_RESPONSE_INVALID);
            }
            return body;
        } catch (HttpStatusCodeException e) {
            log.error("Kakao token request failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);

        } catch (ResourceAccessException e) {
            log.error("Kakao token request network error: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_TIMEOUT);

        } catch (RestClientException e) {
            log.error("Kakao token request failed. code={}, msg={}", code, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }
    }

    public KakaoLoginResponse.KakaoUserInfo requestUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoLoginResponse.KakaoUserInfo> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request, KakaoLoginResponse.KakaoUserInfo.class);
            KakaoLoginResponse.KakaoUserInfo body = response.getBody();
            if(body == null) {
                throw new CustomException(ErrorCode.KAKAO_USERINFO_RESPONSE_INVALID);
            }
            return body;
        } catch (HttpStatusCodeException e) {
            log.error("Kakao userinfo request failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(ErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        } catch (ResourceAccessException e) {
            log.error("Kakao userinfo request network error: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.KAKAO_USERINFO_REQUEST_TIMEOUT);

        } catch (RestClientException e) {
            log.error("Kakao userinfo request failed. msg={}", e.getMessage(), e);
            throw new CustomException(ErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
        }
    }

    public void unlinkByAdminKey(String providerUserId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK "+kakaoAuthProperties.adminKey());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("target_id_type", "user_id");
        params.add("target_id", providerUserId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            restTemplate.exchange(UNLINK_URL, HttpMethod.POST, request, String.class);
        } catch (HttpStatusCodeException e) {
            log.error("Kakao unlink failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
        } catch (RestClientException e) {
            log.error("Kakao unlink failed. msg={}", e.getMessage(), e);
            throw new CustomException(ErrorCode.KAKAO_UNLINK_FAILED);
        }
    }
}
