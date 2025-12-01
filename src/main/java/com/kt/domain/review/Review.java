package com.kt.domain.review;

import com.kt.common.jpa.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Review extends BaseTimeEntity {
	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false)
	private Integer rating;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	private String reviewImageUrl;

	public static Review create(Long userId, Long productId, Integer rating, String content, String reviewImageUrl) {
		Review review = new Review();
		review.userId = userId;
		review.productId = productId;
		review.rating = rating;
		review.content = content;
		review.reviewImageUrl = reviewImageUrl;
		return review;
	}

	public void update(Integer rating, String content, String reviewImageUrl) {
		this.rating = rating;
		this.content = content;
		this.reviewImageUrl = reviewImageUrl;
	}

	public boolean isWriter(Long accessUserId) {
		return this.userId.equals(accessUserId);
	}
}
