package com.kt.controller.user;


import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.user.UserResponse;
import com.kt.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
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
}
