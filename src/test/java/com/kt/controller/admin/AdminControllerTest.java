package com.kt.controller.admin;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AdminControllerTest extends AbstractRestDocsTest {

    private static final String ADMIN_URL_PREFIX = "/admins";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long adminId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User admin = User.admin(
                "admin_login",
                "encoded-password",
                "테스트관리자",
                "admin@test.com",
                "010-9999-9999",
                LocalDate.of(1990, 1, 1),
                Gender.FEMALE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        adminId = userRepository.save(admin).getId();
    }


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
                                    ADMIN_URL_PREFIX+"/signup",
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

    @Nested
    class 관리자_목록_조회_API {
        @Test
        void 성공()  throws Exception {
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            ADMIN_URL_PREFIX,
                            null,
                            HttpMethod.GET,
                            objectMapper
                    ).with(jwtAdmin())
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(adminId))
                    .andExpect(jsonPath("$.data[0].loginId").value("admin_login"))
                    .andExpect(jsonPath("$.data[0].name").value("테스트관리자"))
                    .andExpect(jsonPath("$.data[0].email").value("admin@test.com"))
                    .andExpect(jsonPath("$.data[0].phone").value("010-9999-9999"))
                    .andDo(
                            restDocsFactory.success(
                                    "admin-admins-get-list",
                                    "관리자 목록 조회",
                                    "관리자가 전체 관리자 목록을 조회하는 API",
                                    "Admin",
                                    null,
                                    UserResponse[].class
                            )
                    );
        }
    }

    @Nested
    class 관리자_상세_조회_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_URL_PREFIX + "/" + adminId,
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(adminId))
                    .andExpect(jsonPath("$.data.loginId").value("admin_login"))
                    .andExpect(jsonPath("$.data.name").value("테스트관리자"))
                    .andExpect(jsonPath("$.data.email").value("admin@test.com"))
                    .andExpect(jsonPath("$.data.phone").value("010-9999-9999"))
                    .andDo(
                            restDocsFactory.success(
                                    "admin-admins-get-one",
                                    "관리자 단건 조회",
                                    "관리자가 특정 관리자 정보를 조회하는 API",
                                    "Admin",
                                    null,
                                    UserResponse.class
                            )
                    );
        }
    }

    @Nested
    class 관리자_정보_수정_API {

        @Test
        void 성공() throws Exception {
            // given
            UserRequest.Update request = new UserRequest.Update(
                    "수정된관리자이름",
                    "updated-admin@example.com",
                    "010-8888-7777",
                    LocalDate.of(1995, 5, 5)
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_URL_PREFIX + "/" + adminId,
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(adminId))
                    .andExpect(jsonPath("$.data.name").value(request.name()))
                    .andExpect(jsonPath("$.data.email").value(request.email()))
                    .andExpect(jsonPath("$.data.phone").value(request.phone()))
                    .andExpect(jsonPath("$.data.birthday").value("1995-05-05"))
                    .andDo(
                            restDocsFactory.success(
                                    "admin-admins-update",
                                    "관리자 정보 수정",
                                    "관리자가 특정 관리자 정보를 수정하는 API",
                                    "Admin",
                                    request,
                                    UserResponse.class
                            )
                    );
        }
    }

    @Nested
    class 관리자_삭제_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    ADMIN_URL_PREFIX + "/" + adminId,
                                    null,
                                    HttpMethod.DELETE,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-admins-delete",
                                    "관리자 삭제",
                                    "관리자가 특정 관리자 계정을 삭제(soft delete)하는 API",
                                    "Admin",
                                    null,
                                    null
                            )
                    );
        }
    }

    @Nested
    class 관리자_비밀번호_초기화_API {
        @Test
        void 성공()  throws Exception {
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            ADMIN_URL_PREFIX+"/"+adminId+"/init-password",
                            null,
                            HttpMethod.PATCH,
                            objectMapper
                    ).with(jwtAdmin())
            )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-admins-init-password",
                                    "관리자 비밀번호 초기화",
                                    "관리자가 특정 관리자 계정의 비밀번호를 임시 비밀번호로 초기화하는 API",
                                    "Admin",
                                    null,
                                    null
                            )
                    );

            User updated = userRepository.findById(adminId).orElseThrow();

            assertThat (passwordEncoder.matches("encoded-password", updated.getPassword())).isFalse();
            assertThat(passwordEncoder.matches("AdminPassword123!", updated.getPassword())).isTrue();
        }
    }
}