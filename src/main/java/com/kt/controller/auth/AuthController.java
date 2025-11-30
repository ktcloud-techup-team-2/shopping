package com.kt.controller.auth;

import com.kt.dto.auth.LoginRequest;
import com.kt.dto.auth.LoginResponse;
import com.kt.security.dto.TokenReissueRequestDto;
import com.kt.security.dto.TokenResponseDto;
import com.kt.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue (@RequestBody TokenReissueRequestDto request) {
        TokenResponseDto response = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}
