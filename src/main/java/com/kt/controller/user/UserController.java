package com.kt.controller.user;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.security.AuthUser;
import com.kt.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseEntity<Void> signUp (@RequestBody @Valid UserRequest.Create request){
        userService.signup(request);
        return ApiResponseEntity.created((Void) null);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseEntity<UserResponse> getInfo(@AuthenticationPrincipal AuthUser authUser){
        UserResponse response = userService.getUser(authUser.id());
        return ApiResponseEntity.success(response);
    }

    @PatchMapping("/me")
    public ApiResponseEntity<UserResponse> updateInfo (@AuthenticationPrincipal AuthUser authUser,
                                                       @RequestBody @Valid UserRequest.Update request){
        UserResponse response = userService.updateUser(authUser.id(), request);
        return ApiResponseEntity.success(response);
    }

    @DeleteMapping("/me")
    public ApiResponseEntity<Void> deleteMyInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.deleteUser(authUser.id());
        return ApiResponseEntity.empty();
    }

}
