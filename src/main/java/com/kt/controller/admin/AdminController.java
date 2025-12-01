package com.kt.controller.admin;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.user.UserRequest;
import com.kt.service.admin.AdminService;
import com.kt.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseEntity<Void> signUp (@RequestBody @Valid UserRequest.Create request){
        adminService.signup(request);
        return ApiResponseEntity.created((Void) null);
    }
}
