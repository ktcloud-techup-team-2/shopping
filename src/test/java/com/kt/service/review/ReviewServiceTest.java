package com.kt.service.review;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.review.Review;
import com.kt.dto.review.ReviewRequest;
import com.kt.repository.review.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@InjectMocks
	private ReviewService reviewService;

	@Mock
	private ReviewRepository reviewRepository;

	@Nested
	@DisplayName("리뷰 작성")
	class CreateReview {
		@Test
		void 성공() {
			// given
			var request = new ReviewRequest.Create(100L, 5, "최고", null);

			// 저장된 객체 반환 Mock (ID 세팅)
			given(reviewRepository.save(any(Review.class))).willAnswer(inv -> {
				Review r = inv.getArgument(0);
				ReflectionTestUtils.setField(r, "id", 1L);
				return r;
			});

			// when
			var response = reviewService.createReview(1L, request);

			// then
			assertThat(response.reviewId()).isEqualTo(1L);
			assertThat(response.rating()).isEqualTo(5);
			verify(reviewRepository).save(any(Review.class));
		}
	}

	@Nested
	@DisplayName("리뷰 수정")
	class UpdateReview {
		@Test
		@DisplayName("성공: 작성자 본인이면 수정 가능하다")
		void success() {
			// given
			Long userId = 1L;
			Long reviewId = 10L;
			Review review = Review.create(userId, 100L, 5, "원본", null); // 작성자 ID=1

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

			var request = new ReviewRequest.Update(3, "수정됨", null);

			// when
			var response = reviewService.updateReview(userId, reviewId, request);

			// then
			assertThat(response.content()).isEqualTo("수정됨");
			assertThat(response.rating()).isEqualTo(3);
		}

		@Test
		@DisplayName("실패: 작성자가 아니면 수정할 수 없다 (권한 없음)")
		void fail_not_writer() {
			// given
			Long writerId = 1L;
			Long otherUserId = 2L; // 다른 사람
			Long reviewId = 10L;

			Review review = Review.create(writerId, 100L, 5, "원본", null);

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

			var request = new ReviewRequest.Update(3, "수정 시도", null);

			// when & then
			assertThatThrownBy(() -> reviewService.updateReview(otherUserId, reviewId, request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_DENIED);
		}
	}

	@Nested
	@DisplayName("리뷰 삭제")
	class DeleteReview {
		@Test
		@DisplayName("성공: 작성자 본인이면 삭제 가능하다")
		void success() {
			// given
			Long userId = 1L;
			Long reviewId = 10L;
			Review review = Review.create(userId, 100L, 5, "원본", null);

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

			// when
			reviewService.deleteReview(userId, reviewId);

			// then
			verify(reviewRepository).delete(review);
		}

		@Test
		@DisplayName("실패: 작성자가 아니면 삭제할 수 없다")
		void fail_not_writer() {
			// given
			Long writerId = 1L;
			Long otherUserId = 2L;
			Long reviewId = 10L;
			Review review = Review.create(writerId, 100L, 5, "원본", null);

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

			// when & then
			assertThatThrownBy(() -> reviewService.deleteReview(otherUserId, reviewId))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_DENIED);
		}
	}

	@Nested
	@DisplayName("관리자 리뷰 삭제")
	class AdminDelete {
		@Test
		@DisplayName("성공: 관리자는 작성자가 아니어도 삭제 가능하다")
		void success() {
			// given
			Long reviewId = 10L;
			Review review = Review.create(1L, 100L, 5, "원본", null); // 작성자는 1번 유저

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

			// when
			reviewService.deleteReviewByAdmin(reviewId); // 관리자 삭제 호출

			// then
			verify(reviewRepository).delete(review); // 본인 확인 없이 바로 삭제됨
		}
	}
}