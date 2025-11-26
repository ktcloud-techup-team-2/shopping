package com.kt.service.auth;

import com.kt.common.api.ErrorCode;
import com.kt.common.api.CustomException;
import com.kt.domain.user.User;
import com.kt.dto.auth.LoginRequest;
import com.kt.dto.auth.LoginResponse;
import com.kt.repository.user.UserRepository;
import com.kt.security.TokenProvider;
import com.kt.security.dto.TokenRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByLoginId(loginRequest.loginId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);

        TokenRequestDto tokenDto = tokenProvider.generateToken(authentication, user.getId());

        return LoginResponse.of(
                tokenDto.accessToken(),
                user.getId(),
                user.getLoginId()
        );

    }
}
