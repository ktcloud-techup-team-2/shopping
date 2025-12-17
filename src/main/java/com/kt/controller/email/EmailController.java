package com.kt.controller.email;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.email.EmailResponse;
import com.kt.dto.email.EmailRequest;
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
    public ApiResponseEntity<EmailResponse.AuthenticationResponse> sendAuthEmail(@RequestBody @Valid EmailRequest.VerificationRequest request) {
        EmailResponse.AuthenticationResponse response = emailService.sendEmail(request.email());
        return ApiResponseEntity.success(response);
    }

    @PostMapping
    public ApiResponseEntity<Boolean> verifyCode(@RequestBody @Valid EmailRequest.VerificationConfirmRequest request) {
        boolean result = emailService.verifyCode(request.email(), request.code());
        return ApiResponseEntity.success(result);
    }
}
