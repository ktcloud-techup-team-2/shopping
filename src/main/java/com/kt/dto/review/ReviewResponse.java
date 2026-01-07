package com.kt.dto.review;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kt.domain.review.Review;
import com.kt.utils.PrivacyUtils;

public record ReviewResponse(
	Long reviewId,
	Long userId,
	String authorName,
	Long productId,
	Integer rating,
	String content,
	String reviewImageUrl,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime createdAt,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime updatedAt
) {
	public static ReviewResponse from(Review review, String realName) {
		return new ReviewResponse(
			review.getId(),
			review.getUserId(),
			PrivacyUtils.maskName(realName),
			review.getProductId(),
			review.getRating(),
			review.getContent(),
			review.getReviewImageUrl(),
			review.getCreatedAt(),
			review.getUpdatedAt()
		);
	}
}