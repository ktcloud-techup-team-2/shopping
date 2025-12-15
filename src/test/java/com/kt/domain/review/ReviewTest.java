package com.kt.domain.review;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewTest {

	@Test
	@DisplayName("리뷰 생성 성공")
	void create_success() {
		// when
		Review review = Review.create(1L, 100L, 5, "좋아요", "img.jpg");

		// then
		assertThat(review.getUserId()).isEqualTo(1L);
		assertThat(review.getProductId()).isEqualTo(100L);
		assertThat(review.getRating()).isEqualTo(5);
		assertThat(review.getContent()).isEqualTo("좋아요");
		assertThat(review.getReviewImageUrl()).isEqualTo("img.jpg");
	}

	@Test
	@DisplayName("리뷰 수정 성공")
	void update_success() {
		// given
		Review review = Review.create(1L, 100L, 5, "좋아요", "img.jpg");

		// when
		review.update(3, "보통이에요", "new_img.jpg");

		// then
		assertThat(review.getRating()).isEqualTo(3);
		assertThat(review.getContent()).isEqualTo("보통이에요");
		assertThat(review.getReviewImageUrl()).isEqualTo("new_img.jpg");
	}

	@Test
	@DisplayName("작성자 검증 로직 확인 (isWriter)")
	void isWriter_test() {
		// given
		Long writerId = 1L;
		Review review = Review.create(writerId, 100L, 5, "내용", null);

		// then
		assertThat(review.isWriter(writerId)).isTrue();      // 본인 O
		assertThat(review.isWriter(999L)).isFalse(); // 본인 X
	}
}