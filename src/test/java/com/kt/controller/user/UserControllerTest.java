package com.kt.controller.user;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class UserControllerTest extends AbstractRestDocsTest {

    private static final String SIGNUP_URL = "/users/signup";
    private static final String ME_URL = "/users/me";

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;

    @Nested
    class 회원가입_API {

        @Test
        void 성공 () throws Exception {
            // given
            UserRequest.Create request = new UserRequest.Create(
                    "idfortest123",
                    "PasswordTest123!",
                    "PasswordTest123!",
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

    @Nested
    class 유저_정보_조회_API {
        @Test
        void 성공 () throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    "/users/me",
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtUser())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.loginId").value("test1234"))
                    .andExpect(jsonPath("$.data.name").value("조수연"))
                    .andExpect(jsonPath("$.data.email").value("example123@gmail.com"))
                    .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                    .andDo(
                            restDocsFactory.success(
                                    "users-me",
                                    "내 정보 조회",
                                    "현재 로그인한 사용자의 정보를 조회하는 API",
                                    "User",
                                    null,
                                    UserResponse.class
                            )
                    );
        }
    }

    @Nested
    class 유저_정보_수정_API {
        @Test
        void 성공() throws Exception {
            // given
            UserRequest.Update request = new UserRequest.Update(
                    "수정된이름",
                    "updated@example.com",
                    "010-9999-9999",
                    LocalDate.of(2000, 1, 1)
            );
            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ME_URL,
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper
                            ).with(jwtUser())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.loginId").value("test1234"))
                    .andExpect(jsonPath("$.data.name").value(request.name()))
                    .andExpect(jsonPath("$.data.email").value(request.email()))
                    .andExpect(jsonPath("$.data.phone").value(request.phone()))
                    .andExpect(jsonPath("$.data.birthday").value("2000-01-01"))
                    .andDo(
                            restDocsFactory.success(
                                    "users-me-update",
                                    "내 정보 수정",
                                    "현재 로그인한 사용자의 정보를 수정하는 API",
                                    "User",
                                    request,
                                    UserResponse.class
                            )
                    );
        }

        @Test
        void 실패_인증_없음 () throws Exception {
            // given
            UserRequest.Update request = new UserRequest.Update(
                    "수정된이름",
                    "updated@example.com",
                    "010-9999-9999",
                    LocalDate.of(2000, 1, 1)
            );

            // when & then (인증 토큰 없이 호출)
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ME_URL,
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class 유저_탈퇴_API {

        @Test
        void 성공 () throws Exception {
            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ME_URL,
                                    null,
                                    HttpMethod.DELETE,
                                    objectMapper
                            ).with(jwtUser())
                    )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "users-me-delete",
                                    "내 정보 탈퇴",
                                    "현재 로그인한 사용자의 계정을 탈퇴(soft delete)하는 API",
                                    "User",
                                    null,
                                    null
                            )
                    );

        }
    }
}
