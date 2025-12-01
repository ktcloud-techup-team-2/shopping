package com.kt.controller.review;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*; // csrf, user
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.dto.review.ReviewResponse;
import com.kt.service.review.ReviewService;

class AdminReviewControllerTest extends AbstractRestDocsTest {

	private static final String BASE_URL = "/api/v1/admin/reviews";

	@Autowired
	private RestDocsFactory restDocsFactory;

	@MockitoBean
	private ReviewService reviewService;

	@Nested
	@DisplayName("관리자 전체 리뷰 조회 API")
	class GetAllReviews {
		@Test
		void 성공() throws Exception {
			// given
			var realResponse = new ReviewResponse(
				1L, 1L, 100L, 5, "관리자용", null, LocalDateTime.now(), LocalDateTime.now()
			);
			Page<ReviewResponse> page = new PageImpl<>(List.of(realResponse));

			given(reviewService.getAllReviews(any(Pageable.class))).willReturn(page);

			// Shadow DTO + PageResponse (JSON 구조 맞춤)
			var shadowList = List.of(TestReviewResponse.from(realResponse));
			var docsResponse = TestPageResponse.of(shadowList);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
						// ✅ [핵심] 관리자 권한 부여 (표준 방식)
						.with(user("admin").roles("ADMIN"))
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-review-list",
						"전체 리뷰 조회",
						"관리자가 모든 리뷰를 페이징 조회합니다.",
						"Admin-Review",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("관리자 리뷰 강제 삭제 API")
	class DeleteReviewByAdmin {
		@Test
		void 성공() throws Exception {
			Long reviewId = 1L;
			willDoNothing().given(reviewService).deleteReviewByAdmin(reviewId);

			mockMvc.perform(
					restDocsFactory.createRequest(
							BASE_URL + "/{reviewId}",
							null,
							HttpMethod.DELETE,
							objectMapper,
							reviewId
						)
						// ✅ [핵심] 관리자 권한 + CSRF 토큰
						.with(user("admin").roles("ADMIN"))
						.with(csrf())
				)
				.andExpect(status().isNoContent())
				.andDo(
					restDocsFactory.success(
						"admin-review-delete",
						"리뷰 강제 삭제",
						"관리자가 부적절한 리뷰를 삭제합니다.",
						"Admin-Review",
						null,
						null
					)
				);
		}
	}

	// --- Shadow DTOs (이전과 동일) ---
	static class TestReviewResponse {
		Long reviewId; Long userId; Long productId; Integer rating; String content;
		String reviewImageUrl; String createdAt; String updatedAt;

		static TestReviewResponse from(ReviewResponse real) {
			TestReviewResponse dto = new TestReviewResponse();
			dto.reviewId = real.reviewId(); dto.userId = real.userId(); dto.productId = real.productId();
			dto.rating = real.rating(); dto.content = real.content();
			dto.reviewImageUrl = real.reviewImageUrl();
			dto.createdAt = real.createdAt().toString();
			dto.updatedAt = real.updatedAt().toString();
			return dto;
		}
	}

	static class TestPageResponse<T> {
		List<T> data; TestPageInfo page;
		static <T> TestPageResponse<T> of(List<T> data) {
			TestPageResponse<T> r = new TestPageResponse<>();
			r.data = data; r.page = new TestPageInfo();
			return r;
		}
	}
	static class TestPageInfo {
		int number=0; int size=1; long totalElements=1; int totalPages=1;
		boolean hasNext=false; boolean hasPrev=false; List<String> sort=List.of();
	}
}