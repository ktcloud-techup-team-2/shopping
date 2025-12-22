package com.kt.controller.user;


import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.security.AuthUser;
import com.kt.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/super-admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ApiResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> response = userService.getUsers();
        return ApiResponseEntity.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse response = userService.getUser(id);
        return ApiResponseEntity.success(response);
    }

    @PatchMapping("/{id}")
    public ApiResponseEntity<UserResponse> updateInfo (@PathVariable Long id,
                                                       @RequestBody @Valid UserRequest.Update request){
        UserResponse response = userService.updateUser(id, request);
        return ApiResponseEntity.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponseEntity<Void> deleteMyInfo(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        userService.deleteUser(id, accessToken);
        return ApiResponseEntity.empty();
    }

    @PatchMapping("/{id}/change-password")
    public ApiResponseEntity<Void> changeUserPasswordByAdmin (
            @PathVariable Long id,
            @RequestBody @Valid UserRequest.AdminPasswordChange request) {
        userService.changePasswordByAdmin(id, request);
        return ApiResponseEntity.empty();
    }

    @PatchMapping("/{id}/init-password")
    public ApiResponseEntity<Void> initUserPassword (@PathVariable Long id) {
        userService.initPassword(id);
        return ApiResponseEntity.empty();
    }

    @PatchMapping ("/{id}/in-activate")
    public ApiResponseEntity<Void> activateUser(@PathVariable Long id){
        userService.inactivateUser(id);
        return ApiResponseEntity.empty();
    }
}
