package com.kt.service.admin;

import com.kt.common.Preconditions;
import com.kt.common.api.ErrorCode;
import com.kt.domain.user.User;
import com.kt.dto.user.UserRequest;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserRequest.Create request) {
        Preconditions.validate(request.password().equals(request.passwordConfirm()), ErrorCode.INVALID_PASSWORD_CHECK);
        Preconditions.validate(!userRepository.existsByloginId(request.loginId()), ErrorCode.INVALID_USER_ID);

        var user = User.admin(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.email(),
                request.phone(),
                request.birthday(),
                request.gender(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userRepository.save(user);
    }
}
