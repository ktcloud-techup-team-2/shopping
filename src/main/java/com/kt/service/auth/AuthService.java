package com.kt.service.auth;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.user.User;
import com.kt.dto.auth.*;
import com.kt.dto.email.EmailResponse;
import com.kt.repository.user.UserRepository;
import com.kt.security.TokenProvider;
import com.kt.security.dto.TokenRequestDto;
import com.kt.security.dto.TokenResponseDto;
import com.kt.service.email.EmailService;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private static final String PW_RESET_PRIFIX = "pwReset:";
    private static final Duration PW_RESET_TTL = Duration.ofMinutes(10);

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByLoginIdAndDeletedAtIsNull(loginRequest.loginId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);

        TokenRequestDto tokenDto = tokenProvider.generateToken(authentication, user.getId());

        String redisKey = "refreshToken:"+user.getId();

        redisTemplate.opsForValue().set(
                redisKey,
                tokenDto.refreshToken(),
                Duration.ofDays(7)
        );

        return LoginResponse.of(
                tokenDto.accessToken(),
                user.getId(),
                user.getLoginId()
        );
    }

    public TokenResponseDto reissue(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new JwtException(ErrorCode.JWT_DECODE_FAIL.getMessage());
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        String redisKey = "refreshToken:"+userId;
        String token = redisTemplate.opsForValue().get(redisKey);

        if (token == null || !token.equals(refreshToken)) {
            throw new CustomException(ErrorCode.JWT_SIGNATURE_FAIL);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

        TokenRequestDto tokenDto = tokenProvider.generateToken(authentication, userId);

        redisTemplate.opsForValue().set(redisKey, tokenDto.refreshToken(), Duration.ofDays(7));

        return TokenResponseDto.of(tokenDto.accessToken(), userId);
    }

    public void logout(Long userId, String accessToken) {

        Preconditions.validate(tokenProvider.validateToken(accessToken), ErrorCode.UNAUTHORIZED_CLIENT);

        String refreshKey = "refreshToken:" + userId;
        redisTemplate.delete(refreshKey);

        long remainingTime = tokenProvider.getRemainingValidity(accessToken);

        if(remainingTime > 0) {
            String blacklistKey = "blacklist:" + accessToken;
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    "logout",
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public FindIdResponse findLoginId (FindIdRequest request) {
        User user = userRepository.findByEmailAndNameAndDeletedAtIsNull(request.email(), request.name())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        emailService.sendLoginIdEmail(user.getEmail(), user.getName(), user.getLoginId());

        return FindIdResponse.ok();
    }

    public EmailResponse.AuthenticationResponse requestPasswordReset(FindPasswordRequest request) {
        userRepository.findByLoginIdAndEmailAndNameAndDeletedAtIsNull(request.loginId(), request.email(), request.name())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        EmailResponse.AuthenticationResponse response = emailService.sendAuthenticationEmail(request.email());

        return response;
    }

    public PasswordResetTokenResponse verifyPasswordResetCode(String email, String code) {
        boolean verified = emailService.verifyCode(email, code);

        Preconditions.validate(verified, ErrorCode.EMAIL_AUTH_CODE_INVALID);

        String resetToken = UUID.randomUUID().toString();

        String key = PW_RESET_PRIFIX + resetToken;
        redisTemplate.opsForValue().set(key, email, PW_RESET_TTL);

        return PasswordResetTokenResponse.of(resetToken);
    }

    public void updatePassword(ResetPasswordRequest request) {
        Preconditions.validate(request.newPassword().equals(request.newPasswordConfirm()), ErrorCode.INVALID_PASSWORD_CHECK);

        String key = PW_RESET_PRIFIX + request.resetToken();
        String email = redisTemplate.opsForValue().get(key);

        Preconditions.nullValidate(email, ErrorCode.PASSWORD_RESET_TOKEN_INVALID);

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(request.newPassword()));

        redisTemplate.delete(key);
    }
}