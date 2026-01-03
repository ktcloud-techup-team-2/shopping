package com.kt.controller.auth;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.auth.*;
import com.kt.dto.email.EmailRequest;
import com.kt.dto.email.EmailResponse;
import com.kt.security.AuthUser;
import com.kt.security.dto.TokenReissueRequestDto;
import com.kt.security.dto.TokenResponseDto;
import com.kt.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponseEntity.success(response);
    }

    @PostMapping("/reissue")
    public ApiResponseEntity<TokenResponseDto> reissue (@RequestBody TokenReissueRequestDto request) {
        TokenResponseDto response = authService.reissue(request.refreshToken());
        return ApiResponseEntity.success(response);
    }

    @PostMapping("/logout")
    public ApiResponseEntity<Void> logout(@AuthenticationPrincipal AuthUser user,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        authService.logout(user.id(), accessToken);

        return ApiResponseEntity.empty();
    }

    @PostMapping("/find-id")
    public ApiResponseEntity<FindIdResponse> findId (@RequestBody @Valid FindIdRequest request) {
        FindIdResponse response = authService.findLoginId(request);
        return ApiResponseEntity.success(response);
    }

    @PostMapping("/reset-password/request")
    public ApiResponseEntity<EmailResponse.AuthenticationResponse> requestResetPassword(@RequestBody @Valid FindPasswordRequest request) {
        EmailResponse.AuthenticationResponse response = authService.requestPasswordReset(request);
        return ApiResponseEntity.success(response);
    }

    @PostMapping("/reset-password/verify")
    public ApiResponseEntity<PasswordResetTokenResponse> verifyResetCode(@RequestBody @Valid EmailRequest.VerificationConfirmRequest request) {
        PasswordResetTokenResponse response = authService.verifyPasswordResetCode(request.email(), request.code());
        return ApiResponseEntity.success(response);
    }

    @PostMapping("/update-password")
    public ApiResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.updatePassword(request);
        return ApiResponseEntity.empty();
    }

    @GetMapping("/login/kakao")
    public ApiResponseEntity<LoginResponse> kakaoLogin(@RequestParam("code") String code) {
        LoginResponse response = authService.kakaoLogin(code);
        return ApiResponseEntity.success(response);
    }
}