package com.kt.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewRequest {

	public record Create(
		@NotNull(message = "상품 ID는 필수입니다.")
		Long productId,

		@NotNull(message = "별점은 필수입니다.")
		@Min(1) @Max(5)
		Integer rating,

		@NotBlank(message = "리뷰 내용은 필수입니다.")
		String content,

		String reviewImageUrl
	) {}

	public record Update(
		@NotNull(message = "별점은 필수입니다.")
		@Min(1) @Max(5)
		Integer rating,

		@NotBlank(message = "리뷰 내용은 필수입니다.")
		String content,

		String reviewImageUrl
	) {}
}