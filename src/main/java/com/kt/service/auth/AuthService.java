package com.kt.service.auth;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.user.User;
import com.kt.dto.auth.LoginRequest;
import com.kt.dto.auth.LoginResponse;
import com.kt.repository.user.UserRepository;
import com.kt.security.TokenProvider;
import com.kt.security.dto.TokenRequestDto;
import com.kt.security.dto.TokenResponseDto;
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
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

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
        if (!tokenProvider.validateToken(accessToken)) {
            throw new JwtException(ErrorCode.UNAUTHORIZED_CLIENT.getMessage());
        }

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
}