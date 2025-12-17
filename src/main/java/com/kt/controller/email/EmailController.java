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
public class EmailController {}
