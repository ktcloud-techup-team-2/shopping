package com.kt.controller.auth;

import com.jayway.jsonpath.JsonPath;
import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.oauth.KakaoUtil;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.auth.*;
import com.kt.dto.auth.oauth.KakaoLoginResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class AuthControllerTest extends AbstractRestDocsTest {

    private static final String LOGIN_URL = "/auth/login";
    private static final String REISSUE_URL = "/auth/reissue";
    private static final String LOGOUT_URL = "/auth/logout";
    private static final String FIND_ID_URL = "/auth/find-id";
    private static final String RESET_PASSWORD_REQUEST_URL = "/auth/reset-password/request";
    private static final String RESET_PASSWORD_VERIFY_URL = "/auth/reset-password/verify";
    private static final String UPDATE_PASSWORD_URL = "/auth/update-password";
    private static final String KAKAO_LOGIN_URL = "/auth/login/kakao";

    private static final String LOGIN_ID = "loginUser123";
    private static final String PASSWORD = "PasswordTest123!";

    private static final String ADMIN_LOGIN_ID  = "adminUser123";
    private static final String ADMIN_PASSWORD  = "AdminTest123!";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ValueOperations<String, String> emailValueOperations;

    @MockitoBean
    private SesClient sesClient;

    @MockitoBean
    private SpringTemplateEngine templateEngine;

    @MockitoBean
    private KakaoUtil kakaoUtil;

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

            assertThat(accessToken).isNotBlank();

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    LOGOUT_URL,
                                    null,
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

            String key = "pwReset:" + resetToken;
            given(valueOperations.get(eq(key))).willReturn("login@example.com");

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

    @Nested
    class 카카오_로그인_API {
        @Test
        void 성공_신규유저_회원가입_및_로그인() throws Exception {
            String code = "kakao-auth-code";
            String kakaoAccessToken = "kakao-access-token";

            KakaoLoginResponse.OAuthToken token = stubKakaoToken(code, kakaoAccessToken);
            stubKakaoUserInfo(token.accessToken(), 1234567890L, "login@example.com", "카카오닉네임");

            mockMvc.perform(get(KAKAO_LOGIN_URL).param("code", code))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").isNumber());
        }

        @Test
        void 성공_기존유저_로그인() throws Exception {
            Long kakaoId = 1234567890L;
            String email = "login@example.com";
            String nickname = "카카오닉네임";

            // 첫 번째 호출로 신규 가입
            String firstCode = "kakao-auth-code-1";
            var firstToken = stubKakaoToken(firstCode, "kakao-access-token-1");
            stubKakaoUserInfo(firstToken.accessToken(), kakaoId, email, nickname);

            mockMvc.perform(get(KAKAO_LOGIN_URL).param("code", firstCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").isNumber());

            // 기존유저 로그인
            String secondCode = "kakao-auth-code-2";
            var secondToken = stubKakaoToken(secondCode, "kakao-access-token-2");
            stubKakaoUserInfo(secondToken.accessToken(), kakaoId, email, nickname);

            mockMvc.perform(get(KAKAO_LOGIN_URL).param("code", secondCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").isNumber());
        }

        private KakaoLoginResponse.OAuthToken stubKakaoToken(String code, String kakaoAccessToken) {
            KakaoLoginResponse.OAuthToken token = new KakaoLoginResponse.OAuthToken(
                    "bearer",
                    kakaoAccessToken,
                    3600,
                    "kakao-refresh-token",
                    1209600,
                    "account_email profile_nickname"
            );
            given(kakaoUtil.requestToken(eq(code))).willReturn(token);
            return token;
        }

        private void stubKakaoUserInfo(String kakaoAccessToken, Long kakaoId, String email, String nickname) {
            KakaoLoginResponse.KakaoUserInfo userInfo = new KakaoLoginResponse.KakaoUserInfo(
                    kakaoId,
                    new KakaoLoginResponse.KakaoAccount(email, true),
                    new KakaoLoginResponse.Properties(nickname)
            );
            given(kakaoUtil.requestUserInfo(eq(kakaoAccessToken))).willReturn(userInfo);
        }
    }
}
