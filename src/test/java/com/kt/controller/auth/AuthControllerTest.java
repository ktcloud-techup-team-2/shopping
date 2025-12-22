package com.kt.controller.auth;

import com.jayway.jsonpath.JsonPath;
import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.auth.*;
import com.kt.dto.email.EmailRequest;
import com.kt.dto.email.EmailResponse;
import com.kt.repository.pet.PetRepository;
import com.kt.repository.user.UserRepository;
import com.kt.security.TokenProvider;
import com.kt.security.dto.TokenReissueRequestDto;
import com.kt.security.dto.TokenRequestDto;
import com.kt.security.dto.TokenResponseDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AuthControllerTest extends AbstractRestDocsTest {

    private static final String LOGIN_URL = "/auth/login";
    private static final String REISSUE_URL = "/auth/reissue";
    private static final String LOGOUT_URL = "/auth/logout";
    private static final String FIND_ID_URL = "/auth/find-id";
    private static final String RESET_PASSWORD_REQUEST_URL = "/auth/reset-password/request";
    private static final String RESET_PASSWORD_VERIFY_URL = "/auth/reset-password/verify";
    private static final String UPDATE_PASSWORD_URL = "/auth/update-password";

    private static final String LOGIN_ID = "loginUser123";
    private static final String PASSWORD = "PasswordTest123!";

    private static final String ADMIN_LOGIN_ID  = "adminUser123";
    private static final String ADMIN_PASSWORD  = "AdminTest123!";

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private RedissonClient redissonClient;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ValueOperations<String, String> emailValueOperations;

    @MockitoBean
    private SesClient sesClient;

    @MockitoBean
    private SpringTemplateEngine templateEngine;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PetRepository petRepository;

    private Long userId;
    private Long adminId;

    @BeforeEach
    void setUp() {
        given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willAnswer(inv -> {
            String key = inv.getArgument(0);

            // 이메일 인증코드 검증용
            if (key.startsWith("emailAuth:")) return "123456";

            // 비밀번호 재설정 토큰 검증용
            if (key.startsWith("pwReset:")) return "login@example.com";
            return null;
        });

        // 템플릿 엔진 / SES는 실제 호출 안 하도록 고정 응답
        given(templateEngine.process(anyString(), any(Context.class))).willReturn("mock-email-content");
        given(sesClient.sendEmail(any(SendEmailRequest.class)))
                .willReturn(SendEmailResponse.builder().build());



        SecurityContextHolder.clearContext();
        petRepository.deleteAll();
        userRepository.deleteAll();

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

        User admin = User.admin(
                ADMIN_LOGIN_ID,
                passwordEncoder.encode(ADMIN_PASSWORD),
                "관리자",
                "admin@example.com",
                "010-9999-9999",
                LocalDate.of(1995, 5, 5),
                Gender.MALE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        adminId = userRepository.save(admin).getId();
    }

    @Nested
    class 로그인_API {

        @Test
        void 유저_로그인_성공() throws Exception {

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
        void 관리자_로그인_성공() throws Exception {
            LoginRequest request = new LoginRequest(ADMIN_LOGIN_ID, ADMIN_PASSWORD);

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
                                    "auth-login-admin",
                                    "관리자 로그인",
                                    "관리자 로그인 및 토큰 발급 API",
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
        void 성공_USER() throws Exception {
            reissueSuccessFor(userId, "ROLE_USER", "auth-reissue-user");
        }

        @Test
        void 성공_ADMIN() throws Exception {
            reissueSuccessFor(adminId, "ROLE_ADMIN", "auth-reissue-admin");
        }

        private void reissueSuccessFor(Long targetUserId, String role, String docsId) throws Exception {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    targetUserId,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            TokenRequestDto tokenDto = tokenProvider.generateToken(authentication, targetUserId);
            String refreshToken = tokenDto.refreshToken();

            String redisKey = "refreshToken:" + targetUserId;

            given(valueOperations.get(eq(redisKey))).willReturn(refreshToken);
            doNothing().when(valueOperations).set(eq(redisKey), anyString(), any(Duration.class));

            TokenReissueRequestDto request = new TokenReissueRequestDto(refreshToken);

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    REISSUE_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    docsId,
                                    "토큰 재발급",
                                    role + " accessToken 재발급 API",
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

            LoginRequest loginRequest = new LoginRequest(LOGIN_ID, PASSWORD);

            MvcResult loginResult = mockMvc.perform(
                            restDocsFactory.createRequest(
                                    LOGIN_URL,
                                    loginRequest,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isOk())
                    .andReturn();

            String accessToken = JsonPath.read(
                    loginResult.getResponse().getContentAsString(),
                    "$.data.accessToken"
            );

            // 방어(토큰이 비면 지금처럼 Bearer null 되어 401 남)
            assertThat(accessToken).isNotBlank();

            // 2) logout 호출 (Authorization 헤더에 실제 토큰 넣기)
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    LOGOUT_URL,
                                    null,               // 바디 없음
                                    HttpMethod.POST,
                                    objectMapper
                            ).header("Authorization", "Bearer " + accessToken)
                    )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-logout",
                                    "로그아웃",
                                    "AccessToken을 무효화(블랙리스트) 처리하는 로그아웃 API",
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
    @Nested
    class 아이디_찾기_API {

        @Test
        void 성공() throws Exception {
            FindIdRequest request = new FindIdRequest(
                    "login@example.com",
                    "로그인유저"
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    FIND_ID_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-find-id",
                                    "아이디 찾기",
                                    "이메일로 아이디(로그인 ID) 안내 메일 발송 API",
                                    "Auth",
                                    request,
                                    FindIdResponse.class
                            )
                    );
        }

        @Test
        void 실패_요청값_검증_이메일형식오류() throws Exception {
            FindIdRequest request = new FindIdRequest(
                    "not-an-email",
                    "로그인유저"
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    FIND_ID_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 비밀번호_재설정_인증요청_API {

        @Test
        void 성공() throws Exception {
            FindPasswordRequest request = new FindPasswordRequest(
                    LOGIN_ID,
                    "login@example.com",
                    "로그인유저"
            );

            // sendAuthenticationEmail 내부에서 redisTemplate.opsForValue().set(...) 호출됨
            doNothing().when(emailValueOperations).set(anyString(), anyString(), any());

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    RESET_PASSWORD_REQUEST_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-reset-password-request",
                                    "비밀번호 재설정 인증요청",
                                    "비밀번호 재설정을 위한 이메일 인증번호 발송 API",
                                    "Auth",
                                    request,
                                    EmailResponse.AuthenticationResponse.class
                            )
                    );
        }

        @Test
        void 실패_요청값_검증_필드누락() throws Exception {
            // loginId 빈 값 -> @NotBlank 위반
            FindPasswordRequest request = new FindPasswordRequest(
                    "",
                    "login@example.com",
                    "로그인유저"
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    RESET_PASSWORD_REQUEST_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 비밀번호_재설정_인증번호_검증_API {

        @Test
        void 성공() throws Exception {
            String email = "login@example.com";
            String code = "123456";

            // EmailService.verifyCode(email, code)에서 redisTemplate.opsForValue().get(...) 호출
            given(emailValueOperations.get(anyString())).willReturn(code);
            given(stringRedisTemplate.delete(anyString())).willReturn(true);

            EmailRequest.VerificationConfirmRequest request =
                    new EmailRequest.VerificationConfirmRequest(email, code);

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    RESET_PASSWORD_VERIFY_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-reset-password-verify",
                                    "비밀번호 재설정 인증번호 검증",
                                    "이메일 인증번호 검증 후 비밀번호 재설정 토큰 발급 API",
                                    "Auth",
                                    request,
                                    PasswordResetTokenResponse.class
                            )
                    );
        }

        @Test
        void 실패_요청값_검증_이메일누락() throws Exception {
            EmailRequest.VerificationConfirmRequest request =
                    new EmailRequest.VerificationConfirmRequest("", "123456");

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    RESET_PASSWORD_VERIFY_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 비밀번호_변경_API {

        @Test
        void 성공() throws Exception {
            String resetToken = "test-reset-token";

            // ✅ updatePassword가 조회하는 key 형태와 정확히 맞추기
            String key = "pwReset:" + resetToken;
            given(valueOperations.get(eq(key))).willReturn("login@example.com");

            // ✅ redis delete도 updatePassword에서 수행함 (실제 호출 객체에 맞춰 스텁)
            given(stringRedisTemplate.delete(eq(key))).willReturn(true);

            ResetPasswordRequest request = new ResetPasswordRequest(
                    resetToken,
                    "NewPass123!",
                    "NewPass123!"
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    UPDATE_PASSWORD_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "auth-update-password",
                                    "비밀번호 변경",
                                    "재설정 토큰으로 비밀번호 변경 API",
                                    "Auth",
                                    request,
                                    null
                            )
                    );
        }

        @Test
        void 실패_요청값_검증_비밀번호_패턴불일치() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "test-reset-token",
                    "short1!",
                    "short1!"
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    UPDATE_PASSWORD_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isBadRequest());
        }
    }

}
