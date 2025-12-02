package com.kt.service.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.review.Review;
import com.kt.dto.review.ReviewRequest;
import com.kt.dto.review.ReviewResponse;
import com.kt.repository.review.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

	private final ReviewRepository reviewRepository;

	@Transactional
	public ReviewResponse createReview(Long userId, ReviewRequest.Create request) {

		Review review = Review.create(
			userId,
			request.productId(),
			request.rating(),
			request.content(),
			request.reviewImageUrl()
		);

		return ReviewResponse.from(reviewRepository.save(review));
	}

	public Page<ReviewResponse> getReviewListByProduct(Long productId, Pageable pageable) {
		return reviewRepository.findByProductId(productId, pageable)
			.map(ReviewResponse::from);
	}

	public Page<ReviewResponse> getAllReviews(Pageable pageable) {
		return reviewRepository.findAll(pageable)
			.map(ReviewResponse::from);
	}

	public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest.Update request) {
		Review review = findReviewById(reviewId);

		validateWriter(userId, review);

		review.update(request.rating(), request.content(), request.reviewImageUrl());
		return ReviewResponse.from(review);
	}

	public void deleteReview(Long userId, Long reviewId) {
		Review review = findReviewById(reviewId);
		validateWriter(userId, review);
		reviewRepository.delete(review);
	}

	public void deleteReviewByAdmin(Long reviewId) {
		Review review = findReviewById(reviewId);
		reviewRepository.delete(review);
	}

    public Page<ReviewResponse> getReviewsByUser (Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(ReviewResponse::from);
    }

	private Review findReviewById(Long reviewId) {
		return reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT));
	}

	private void validateWriter(Long userId, Review review) {
		Preconditions.validate(
			review.isWriter(userId),
			ErrorCode.PERMISSION_DENIED
		);
	}
}