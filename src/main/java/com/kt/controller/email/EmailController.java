package com.kt.controller.email;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.auth.EmailRequest;
import com.kt.dto.auth.EmailResponse;
import com.kt.dto.auth.EmailVerificationRequest;
import com.kt.service.email.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/auth-email")
    public ApiResponseEntity<EmailResponse> sendAuthEmail(@RequestBody @Valid EmailRequest request) {
        EmailResponse response = emailService.sendEmail(request.email());
        return ApiResponseEntity.success(response);
    }

    @GetMapping
    public ApiResponseEntity<Boolean> verifyCode(@RequestBody @Valid EmailVerificationRequest request) {
        boolean result = emailService.verifyCode(request.email(), request.code());
        return ApiResponseEntity.success(result);
    }
}
