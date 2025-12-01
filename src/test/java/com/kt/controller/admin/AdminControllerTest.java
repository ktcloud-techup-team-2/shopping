package com.kt.controller.admin;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.dto.user.UserRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AdminControllerTest extends AbstractRestDocsTest {

    private static final String ADMIN_SIGNUP_URL = "/admin/signup";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Nested
    class 관리자_회원가입_API {

        @Test
        void 성공() throws Exception {
            // given
            UserRequest.Create request = new UserRequest.Create(
                    "adminTest123",
                    "PasswordTest123!",
                    "PasswordTest123!",
                    "AdminName",
                    "admin@example.com",
                    "010-1111-2222",
                    Gender.FEMALE,
                    LocalDate.of(1990, 1, 1)
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_SIGNUP_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isCreated())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-signup",
                                    "관리자 회원가입",
                                    "관리자 계정을 신규로 생성하는 API",
                                    "Admin",
                                    request,
                                    null
                            )
                    );
        }
    }
}