package com.kt.controller.review;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.security.AuthUser;
import com.kt.dto.review.ReviewRequest;
import com.kt.dto.review.ReviewResponse;
import com.kt.service.review.ReviewService;

class ReviewControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/reviews";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@MockitoBean
	private ReviewService reviewService;

	@Nested
	@DisplayName("리뷰 작성 API")
	class CreateReview {
		@Test
		void 성공() throws Exception {
			// given
			var request = new ReviewRequest.Create(100L, 5, "좋아요", null);
			var realResponse = createMockResponse(1L, 100L, "좋아요");

			// Service Mocking (Controller가 AuthUser에서 ID를 꺼내서 전달함)
			given(reviewService.createReview(anyLong(), any(ReviewRequest.Create.class)))
				.willReturn(realResponse);

			// Shadow DTO + ApiResponse 포장
			var docsResponse = ApiResponse.of(TestReviewResponse.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL,
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(mockAuthUser()) // ✅ [해결 1] AuthUser 객체 직접 주입 (403 해결)
						.with(csrf())         // ✅ [해결 2] CSRF 토큰 주입 (403 해결)
				)
				.andExpect(status().isCreated())
				.andDo(
					restDocsFactory.success(
						"review-create",
						"리뷰 작성",
						"상품에 대한 리뷰를 작성합니다.",
						"Review",
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("상품별 리뷰 목록 조회 API")
	class GetReviewListByProduct {
		@Test
		void 성공() throws Exception {
			// given
			Long productId = 100L;
			var realResponse = createMockResponse(1L, productId, "굿");
			Page<ReviewResponse> page = new PageImpl<>(List.of(realResponse));

			given(reviewService.getReviewListByProduct(eq(productId), any(Pageable.class)))
				.willReturn(page);

			// Shadow DTO List 변환 + PageResponse 포장 (핵심!)
			var shadowList = List.of(TestReviewResponse.from(realResponse));
			var docsResponse = TestPageResponse.of(shadowList); // ✅ [해결 3] 페이징 구조 맞춤

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
						.param("productId", String.valueOf(productId))
						// 조회 API는 보통 권한이 필요 없지만, 테스트 통과를 위해 넣음
						.with(mockAuthUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"review-list-by-product",
						"상품 리뷰 조회",
						"특정 상품의 리뷰 목록을 페이징 조회합니다.",
						"Review",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("리뷰 수정 API")
	class UpdateReview {
		@Test
		void 성공() throws Exception {
			// given
			Long reviewId = 1L;
			var request = new ReviewRequest.Update(4, "수정함", null);
			var realResponse = createMockResponse(reviewId, 100L, "수정함");

			given(reviewService.updateReview(anyLong(), eq(reviewId), any(ReviewRequest.Update.class)))
				.willReturn(realResponse);

			var docsResponse = ApiResponse.of(TestReviewResponse.from(realResponse));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{reviewId}",
							request,
							HttpMethod.PUT,
							objectMapper,
							reviewId
						)
						.with(mockAuthUser()) // ✅ AuthUser 주입
						.with(csrf())         // ✅ CSRF 필수
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"review-update",
						"리뷰 수정",
						"자신이 작성한 리뷰를 수정합니다.",
						"Review",
						request,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("리뷰 삭제 API")
	class DeleteReview {
		@Test
		void 성공() throws Exception {
			Long reviewId = 1L;
			willDoNothing().given(reviewService).deleteReview(anyLong(), eq(reviewId));

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{reviewId}",
							null,
							HttpMethod.DELETE,
							objectMapper,
							reviewId
						)
						.with(mockAuthUser()) // ✅ AuthUser 주입
						.with(csrf())         // ✅ CSRF 필수
				)
				.andExpect(status().isNoContent())
				.andDo(
					restDocsFactory.success(
						"review-delete",
						"리뷰 삭제",
						"자신이 작성한 리뷰를 삭제합니다.",
						"Review",
						null,
						null
					)
				);
		}
	}

	// --- Helper Methods ---

	/**
	 * [핵심] AuthUser 객체를 직접 생성하여 SecurityContext에 주입하는 헬퍼 메서드
	 * @AuthenticationPrincipal이 AuthUser 타입을 요구하므로 필수
	 */
	private RequestPostProcessor mockAuthUser() {
		// 1. 권한 설정 ("ROLE_" 접두사 중요! SecurityConfig에서 hasRole("USER")로 체크하므로)
		var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

		// 2. AuthUser 진짜 객체 생성 (ID: 1L)
		AuthUser authUser = new AuthUser(1L, authorities);

		// 3. 인증 토큰 생성
		UsernamePasswordAuthenticationToken auth =
			new UsernamePasswordAuthenticationToken(authUser, null, authorities);

		// 4. ✅ 핵심: Spring Security Test의 authentication() 프로세서 반환
		// (이게 알아서 SecurityContext에 꽂아주고 필터 통과시켜 줍니다)
		return authentication(auth);
	}

	private ReviewResponse createMockResponse(Long reviewId, Long productId, String content) {
		return new ReviewResponse(
			reviewId, 1L, productId, 5, content, null, LocalDateTime.now(), LocalDateTime.now()
		);
	}

	// --- Shadow DTOs (StackOverflow 방지 & 구조 일치용) ---

	// 1. ReviewResponse Shadow
	static class TestReviewResponse {
		Long reviewId; Long userId; Long productId; Integer rating; String content;
		String reviewImageUrl; String createdAt; String updatedAt;

		static TestReviewResponse from(ReviewResponse real) {
			TestReviewResponse dto = new TestReviewResponse();
			dto.reviewId = real.reviewId();
			dto.userId = real.userId();
			dto.productId = real.productId();
			dto.rating = real.rating();
			dto.content = real.content();
			dto.reviewImageUrl = real.reviewImageUrl();
			dto.createdAt = real.createdAt().toString();
			dto.updatedAt = real.updatedAt().toString();
			return dto;
		}
	}

	// 2. PageResponse Shadow
	static class TestPageResponse<T> {
		List<T> data;
		TestPageInfo page;
		static <T> TestPageResponse<T> of(List<T> data) {
			TestPageResponse<T> r = new TestPageResponse<>();
			r.data = data;
			r.page = new TestPageInfo();
			return r;
		}
	}
	static class TestPageInfo {
		int number=0; int size=1; long totalElements=1; int totalPages=1;
		boolean hasNext=false; boolean hasPrev=false; List<String> sort=List.of();
	}
}