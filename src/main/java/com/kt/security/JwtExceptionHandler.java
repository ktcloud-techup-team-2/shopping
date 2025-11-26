package com.kt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.common.CustomException;
import com.kt.common.ErrorCode;
import com.kt.common.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionHandler {

    private final ObjectMapper objectMapper;

    public void handle(HttpServletResponse response, ErrorCode errorCode) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(errorCode.getStatus().value());

            ErrorResponse body = ErrorResponse.of(errorCode);

            String json = objectMapper.writeValueAsString(body);
            response.getWriter().write(json);
        } catch (IOException ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR_JWT_RESPONSE);
        }
    }
}
