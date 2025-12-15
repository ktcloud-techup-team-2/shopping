package com.kt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.api.ProblemResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionHandler {

    private final ObjectMapper objectMapper;

    public void handle(HttpServletResponse response, ErrorCode errorCode) {
        try {
            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/problem+json;charset=UTF-8");

            ProblemDetail pd = ProblemResponse.of(errorCode);

            String json = objectMapper.writeValueAsString(pd);
            response.getWriter().write(json);
        } catch (IOException ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR_JWT_RESPONSE);
        }
    }
}