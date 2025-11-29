package com.kt.controller.user;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.dto.user.UserSignUpRequest;
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
public class UserControllerTest extends AbstractRestDocsTest {

    private static final String SIGNUP_URL = "/users/signup";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Nested
    class 회원가입_API {

        @Test
        void 성공 () throws Exception {
            // given
            UserSignUpRequest request = new UserSignUpRequest(
                    "test1234",
                    "Test1234!",
                    "Test1234!",
                    "JNSJ",
                    "example123@example.com",
                    "010-1234-1234",
                    Gender.MALE,
                    LocalDate.of(1999, 9, 9)
            );

            // then -> body 없이 201 created
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            SIGNUP_URL,
                            request,
                            HttpMethod.POST,
                            objectMapper
                    )
            )
                    .andExpect(status().isCreated())
                    .andDo(
                            restDocsFactory.success(
                                    "user-signup",
                                    "회원가입",
                                    "신규 회원을 생성하는 API",
                                    "User",
                                    request,
                                    null
                            )
                    );


        }
    }
}
