package com.kt.controller.auth;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.auth.LoginRequest;
import com.kt.dto.auth.LoginResponse;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserRequest.Create;
import com.kt.repository.user.UserRepository;
import com.kt.security.TokenProvider;
import com.kt.security.dto.TokenReissueRequestDto;
import com.kt.security.dto.TokenRequestDto;
import com.kt.security.dto.TokenResponseDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class AuthControllerTest extends AbstractRestDocsTest {

    private static final String LOGIN_URL = "/auth/login";
    private static final String REISSUE_URL = "/auth/reissue";
    private static final String LOGOUT_URL = "/auth/logout";

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Nested
    class 로그인_API {

        @Test
        void 성공() throws Exception {

            SecurityContextHolder.clearContext();

            // 회원가입 데이터 생성
            UserRequest.Create signUpRequest = new UserRequest.Create(
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

    @Nested
    class 토큰_재발급_API {
        @Test
        void 성공() throws Exception {
            SecurityContextHolder.clearContext();

            UserRequest.Create signUpRequest = new UserRequest.Create(
                    "reissueUser123",
                    "PasswordTest123!",
                    "PasswordTest123!",
                    "JNSJ",
                    "reissue@example.com",
                    "010-1234-5678",
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
                    .andExpect(status().isCreated());

            User user = userRepository.findByLoginId("reissueUser123")
                    .orElseThrow();

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));

            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);

            TokenRequestDto tokenRequestDto = tokenProvider.generateToken(authentication, user.getId());

            String redisKey = "refreshToken:" + user.getId();
            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    tokenRequestDto.refreshToken(),
                    Duration.ofDays(7)
            );

            TokenReissueRequestDto request = new TokenReissueRequestDto(tokenRequestDto.refreshToken());

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
                                    "RefreshToken으로으로 AccessToken 재발급하는 API",
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
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 로그아웃_API {
        @Test
        void 성공()  throws Exception {
            SecurityContextHolder.clearContext();

            UserSignUpRequest signUpRequest = new UserSignUpRequest(
                    "logoutUser123",
                    "PasswordTest123!",
                    "PasswordTest123!",
                    "JNSJ",
                    "logout@example.com",
                    "010-1234-5678",
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
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest("logoutUser123", "PasswordTest123!");

            String responseBody = mockMvc.perform(
                            restDocsFactory.createRequest(
                                    "/auth/login",
                                    loginRequest,
                                    HttpMethod.POST,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
            String accessToken = loginResponse.accessToken();


            mockMvc.perform(
                            post(LOGOUT_URL)
                                    .header("Authorization", "Bearer " + accessToken)
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());
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
