package com.kt.controller.user;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AdminUserControllerTest extends AbstractRestDocsTest {

    private static final String ADMIN_USERS_URL = "/admin/users";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Nested
    class 관리자_유저_단건_조회_API {

        @Test
        void 성공() throws Exception {
            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_USERS_URL + "/1",
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))

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
                    .andExpect(jsonPath("$.data[0].id").value(1L))
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

            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_USERS_URL + "/1",   // id=1 유저 수정
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper
                            ).with(jwtAdmin())              // ✅ ADMIN 권한
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
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
            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_USERS_URL + "/1",   // id=1 유저 삭제
                                    null,
                                    HttpMethod.DELETE,
                                    objectMapper
                            ).with(jwtAdmin())              // ✅ ADMIN 권한
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
}