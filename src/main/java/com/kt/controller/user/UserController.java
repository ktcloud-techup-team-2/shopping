package com.kt.controller.user;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.user.UserResponse;
import com.kt.dto.user.UserSignUpRequest;
import com.kt.security.AuthUser;
import com.kt.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseEntity<Void> signUp (@RequestBody @Valid UserSignUpRequest request){
        userService.signup(request);
        return ApiResponseEntity.created((Void) null);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseEntity<UserResponse> getUser(@AuthenticationPrincipal AuthUser authUser){
        UserResponse response = userService.getUser(authUser.id());
        return ApiResponseEntity.success(response);
    }
}
