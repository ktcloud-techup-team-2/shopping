package com.kt.common;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.domain.user.Role;
import com.kt.security.TokenProvider;
import com.kt.security.dto.TokenRequestDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@ActiveProfiles("test")
public abstract class AbstractRestDocsTest {

	@Autowired
	protected WebApplicationContext context;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected TokenProvider tokenProvider;

	protected MockMvc mockMvc;

	// 테스트용 ID
	protected static final Long DEFAULT_USER_ID = 1L;
	protected static final Long DEFAULT_ADMIN_ID = 10000L;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.apply(springSecurity())
			.apply(documentationConfiguration(restDocumentation))
			.alwaysDo(MockMvcResultHandlers.print())
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.build();
	}

	private String createAccessToken(Long userId, String... roles) {
		var authorities = Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

		Authentication auth = new UsernamePasswordAuthenticationToken(
			userId.toString(),
			"",
			authorities
		);

		TokenRequestDto tokenDto = tokenProvider.generateToken(auth, userId);
		return tokenDto.accessToken();
	}

	/** USER 토큰 */
	protected RequestPostProcessor jwtUser() {
		return request -> {
			String token = createAccessToken(DEFAULT_USER_ID, Role.USER.name());
			request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			return request;
		};
	}

	/** ADMIN 토큰 */
	protected RequestPostProcessor jwtAdmin() {
		return request -> {
			String token = createAccessToken(DEFAULT_ADMIN_ID, Role.ADMIN.name());
			request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			return request;
		};
	}
}
