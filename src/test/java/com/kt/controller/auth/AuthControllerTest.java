package com.kt.controller.auth;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.dto.auth.LoginRequest;
import com.kt.dto.auth.LoginResponse;
import com.kt.dto.user.UserSignUpRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AuthControllerTest extends AbstractRestDocsTest {

    private static final String LOGIN_URL = "/auth/login";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Nested
    class 로그인_API {

        @Test
        void 성공() throws Exception {

            SecurityContextHolder.clearContext();

            // 회원가입 데이터 생성
            UserSignUpRequest signUpRequest = new UserSignUpRequest(
                    "idfortest123",
                    "PasswordTest123!",
                    "PasswordTest123!",
                    "JNSJ",
                    "example123@example.com",
                    "010-1234-1234",
                    Gender.MALE,
                    LocalDate.of(1999, 9, 9)
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    "/users/signup",
                                    signUpRequest,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andDo(print())
                    .andExpect(status().isCreated());

            //given
            LoginRequest request = new LoginRequest(
                    "idfortest123",
                    "PasswordTest123!"
            );

            //when
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            LOGIN_URL,
                            request,
                            HttpMethod.POST,
                            objectMapper
                    )
            )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-login",
                                    "로그인",
                                    "기존 회원의 로그인 및 토큰 발급 API",
                                    "Auth",
                                    request,
                                    LoginResponse.class
                            )
                    );
        }

        @Test
        void 실패_비밀번호_불일치() throws Exception {
            //given
            LoginRequest request = new LoginRequest(
                    "idfortest123",
                    "WrongPassword1!"
            );

            //when
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            LOGIN_URL,
                            request,
                            HttpMethod.POST,
                            objectMapper
                    )
            )
                    .andExpect(status(). isUnauthorized());
        }

        @Test
        void 실패_아이디_불일치() throws Exception {
            // given
            LoginRequest request = new LoginRequest(
                    "wrongId1234",
                    "PasswordTest123!"
            );

            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    LOGIN_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isUnauthorized());
        }
    }
}
