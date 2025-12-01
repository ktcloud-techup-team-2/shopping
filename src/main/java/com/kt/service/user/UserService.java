package com.kt.service.user;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.user.User;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserRequest.Create request) {
        Preconditions.validate(request.password().equals(request.passwordConfirm()), ErrorCode.INVALID_PASSWORD_CHECK);
        Preconditions.validate(!userRepository.existsByloginId(request.loginId()), ErrorCode.INVALID_USER_ID);

        var user = User.user(
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

    @Transactional(readOnly = true)
    public UserResponse getUser (Long userId){
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(UserResponse::from)
                .toList();
    }
    public UserResponse updateUser(Long userId, UserRequest.Update userRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updateInfo (
                userRequest.name(),
                userRequest.email(),
                userRequest.phone(),
                userRequest.birthday()
        );

        userRepository.save(user);

        return UserResponse.from(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        user.softDelete();
    }
}