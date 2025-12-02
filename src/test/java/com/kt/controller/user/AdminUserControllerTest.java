package com.kt.controller.user;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AdminUserControllerTest extends AbstractRestDocsTest {

    private static final String ADMIN_USERS_URL = "/admin/users";

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = User.user(
                "test1234",
                "encoded-password",
                "테스트",
                "example123@gmail.com",
                "010-1234-5678",
                LocalDate.of(2002, 8, 9),
                Gender.FEMALE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userId = userRepository.save(user).getId();
    }

    @Nested
    class 관리자_유저_단건_조회_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_USERS_URL + "/" + userId,
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.loginId").value("test1234"))
                    .andExpect(jsonPath("$.data.name").value("테스트"))
                    .andExpect(jsonPath("$.data.email").value("example123@gmail.com"))
                    .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                    .andDo(
                            restDocsFactory.success(
                                    "admin-users-get-one",
                                    "유저 단건 조회(관리자)",
                                    "관리자가 특정 유저의 정보를 조회하는 API",
                                    "Admin-User",
                                    null,
                                    UserResponse.class
                            )
                    );
        }
    }

    @Nested
    class 관리자_유저_목록_조회_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_USERS_URL,
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(userId))
                    .andDo(
                            restDocsFactory.success(
                                    "admin-users-get-list",
                                    "유저 목록 조회(관리자)",
                                    "관리자가 전체 유저 목록을 조회하는 API",
                                    "Admin-User",
                                    null,
                                    UserResponse[].class
                            )
                    );
        }
    }

    @Nested
    class 관리자_유저_정보_수정_API {

        @Test
        void 성공() throws Exception {
            // given
            UserRequest.Update request = new UserRequest.Update(
                    "관리자수정이름",
                    "updated-admin@example.com",
                    "010-9999-8888",
                    LocalDate.of(1995, 5, 5)
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_USERS_URL + "/" + userId,
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.name").value(request.name()))
                    .andExpect(jsonPath("$.data.email").value(request.email()))
                    .andExpect(jsonPath("$.data.phone").value(request.phone()))
                    .andExpect(jsonPath("$.data.birthday").value("1995-05-05"))
                    .andDo(
                            restDocsFactory.success(
                                    "admin-users-update",
                                    "유저 정보 수정(관리자)",
                                    "관리자가 특정 유저의 정보를 수정하는 API",
                                    "Admin-User",
                                    request,
                                    UserResponse.class
                            )
                    );
        }
    }

    @Nested
    class 관리자_유저_삭제_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_USERS_URL + "/" + userId,
                                    null,
                                    HttpMethod.DELETE,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-users-delete",
                                    "유저 삭제(관리자)",
                                    "관리자가 특정 유저를 탈퇴(soft delete) 처리하는 API",
                                    "Admin-User",
                                    null,
                                    null
                            )
                    );
        }
    }

    @Nested
    class 관리자_유저_비활성화_API {
        @Test
        void 성공 () throws Exception {
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            ADMIN_USERS_URL+"/"+userId+"/in-activate",
                            null,
                            HttpMethod.PATCH,
                            objectMapper
                    ).with(jwtAdmin())
            )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-users-inactivate",
                                    "유저 비활성화 (관리자)",
                                    "관리자가 특정 유저를 비활성화하는 API",
                                    "Admin-user",
                                    null,
                                    null
                            )
                    );
        }
    }
}