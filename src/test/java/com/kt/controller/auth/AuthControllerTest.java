package com.kt.controller.auth;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.auth.LoginRequest;
import com.kt.dto.auth.LoginResponse;
import com.kt.repository.user.UserRepository;
import com.kt.security.TokenProvider;
import com.kt.security.dto.TokenReissueRequestDto;
import com.kt.security.dto.TokenResponseDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AuthControllerTest extends AbstractRestDocsTest {

    private static final String LOGIN_URL = "/auth/login";
    private static final String REISSUE_URL = "/auth/reissue";
    private static final String LOGOUT_URL = "/auth/logout";

    private static final String LOGIN_ID = "loginUser123";
    private static final String PASSWORD = "PasswordTest123!";

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long userId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
        stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();

        User user = User.user(
                LOGIN_ID,
                passwordEncoder.encode(PASSWORD),
                "로그인유저",
                "login@example.com",
                "010-0000-1111",
                LocalDate.of(2000, 1, 1),
                Gender.FEMALE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        userId = userRepository.save(user).getId();
    }

    @Nested
    class 로그인_API {

        @Test
        void 성공() throws Exception {

            LoginRequest request = new LoginRequest(
                    LOGIN_ID,
                    PASSWORD
            );

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
                    LOGIN_ID,
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
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 실패_아이디_불일치() throws Exception {
            // given
            LoginRequest request = new LoginRequest(
                    "wrongId1234",
                    PASSWORD
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

    @Nested
    class 토큰_재발급_API {
        @Test
        void 성공() throws Exception {

            LoginRequest loginRequest = new LoginRequest(LOGIN_ID, PASSWORD);

            String loginResponseBody = mockMvc.perform(
                            restDocsFactory.createRequest(
                                    LOGIN_URL,
                                    loginRequest,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            LoginResponse loginResponse =
                    objectMapper.readValue(loginResponseBody, LoginResponse.class);

            Long loginUserId = loginResponse.userId();

            String redisKey = "refreshToken:" + loginUserId;
            String refreshToken = stringRedisTemplate.opsForValue().get(redisKey);

            TokenReissueRequestDto request = new TokenReissueRequestDto(refreshToken);

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    REISSUE_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-reissue",
                                    "토큰 재발급",
                                    "RefreshToken으로 AccessToken 재발급하는 API",
                                    "Auth",
                                    request,
                                    TokenResponseDto.class
                            )
                    );
        }

        @Test
        void 실패_유효하지_않은_refreshToken() throws Exception {

            TokenReissueRequestDto request = new TokenReissueRequestDto("invalid-refresh-token");
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            REISSUE_URL,
                            request,
                            HttpMethod.POST,
                            objectMapper
                    )
            )
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 로그아웃_API {
        @Test
        void 성공()  throws Exception {

            LoginRequest request = new LoginRequest(LOGIN_ID, PASSWORD);

            String responseBody = mockMvc.perform(
                    restDocsFactory.createRequest(
                            LOGIN_URL,
                            request,
                            HttpMethod.POST,
                            objectMapper
                    )
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            LoginResponse loginResponse =
                    objectMapper.readValue(responseBody, LoginResponse.class);
            String accessToken = loginResponse.accessToken();

            mockMvc.perform(
                    post(LOGOUT_URL)
                            .header("Authorization", "Bearer " + accessToken)
            )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-logout",
                                    "로그아웃",
                                    "AccessToken 블랙리스트 등록 및 RefreshToken 삭제 API",
                                    "Auth",
                                    null,
                                    null
                            )
                    );
        }

        @Test
        void 실패_잘못된_토큰()  throws Exception {

            mockMvc.perform(
                            post(LOGOUT_URL)
                                    .header("Authorization", "Bearer invalid-access-token")

                    ).andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}
