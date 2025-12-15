package com.kt.dto.review;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kt.domain.review.Review;

public record ReviewResponse(
	Long reviewId,
	Long userId,
	Long productId,
	Integer rating,
	String content,
	String reviewImageUrl,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime createdAt,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime updatedAt
) {
	public static ReviewResponse from(Review review) {
		return new ReviewResponse(
			review.getId(),
			review.getUserId(),
			review.getProductId(),
			review.getRating(),
			review.getContent(),
			review.getReviewImageUrl(),
			review.getCreatedAt(),
			review.getUpdatedAt()
		);
	}
}