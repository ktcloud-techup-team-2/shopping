package com.kt.service.user;

import com.kt.common.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.user.User;
import com.kt.dto.user.UserSignUpRequest;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void signup(UserSignUpRequest request) {
        Preconditions.validate(request.password().equals(request.passwordConfirm()), ErrorCode.INVALID_PASSWORD_CHECK);
        Preconditions.validate(!userRepository.existsByloginId(request.loginId()), ErrorCode.INVALID_USER_ID);

        var user = User.user(
            request.loginId(),
            request.password(),
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
