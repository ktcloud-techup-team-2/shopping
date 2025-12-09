package com.kt.service.user;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.user.User;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.repository.user.UserRepository;
import com.kt.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

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

    public void deleteUser(Long userId, String accessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Preconditions.validate(!user.isDeleted(), ErrorCode.USER_NOT_FOUND);

        user.softDelete();

        Preconditions.validate(tokenProvider.validateToken(accessToken), ErrorCode.UNAUTHORIZED_CLIENT);

        String refreshKey = "refreshToken:" + userId;
        redisTemplate.delete(refreshKey);

        long remainingTime = tokenProvider.getRemainingValidity(accessToken);

        if(remainingTime > 0) {
            String blacklistKey = "blacklist:" + accessToken;
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    "deactivated",
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public void changePassword(Long userId, UserRequest.PasswordChange request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Preconditions.validate(passwordEncoder.matches(request.oldPassword(), user.getPassword()), ErrorCode.INVALID_PASSWORD);

        Preconditions.validate(request.newPassword().equals(request.newPasswordConfirm()), ErrorCode.INVALID_PASSWORD_CHECK);

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(encodedPassword);
    }

    public void changePasswordByAdmin (Long userId, UserRequest.AdminPasswordChange request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Preconditions.validate(request.newPassword().equals(request.newPasswordConfirm()), ErrorCode.INVALID_PASSWORD_CHECK);

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(encodedPassword);
    }

    public void initPassword(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String tempPassword = "Temp1234!";  // 임의 비밀번호 설정
        String encodedPassword = passwordEncoder.encode(tempPassword);
        user.updatePassword(encodedPassword);
    }

    public void inactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Preconditions.validate(!user.isDeleted(), ErrorCode.USER_NOT_FOUND);

        user.softDelete();

        String refreshKey = "refreshToken:" + userId;
        redisTemplate.delete(refreshKey);
    }
}