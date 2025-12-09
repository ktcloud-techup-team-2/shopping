package com.kt.controller.auth;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.auth.MailRequest;
import com.kt.dto.auth.MailResponse;
import com.kt.service.auth.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Objects;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
public class MailController {

    private final MailService mailService;

    @PostMapping("/mail-send")
    public ApiResponseEntity<MailResponse> mailSend (@RequestBody @Valid MailRequest request) {
        MailResponse response = mailService.sendMail(request.email());
        return ApiResponseEntity.success(response);
    }

    @GetMapping("/mail-check")
    public ApiResponseEntity<Boolean> mailCheck(@RequestParam int userNumber) {
        boolean isMatch = mailService.checkVerificationCode(userNumber);
        return ApiResponseEntity.success(isMatch);
    }
}
