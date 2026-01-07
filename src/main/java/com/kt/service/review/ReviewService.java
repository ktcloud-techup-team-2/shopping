package com.kt.service.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.review.Review;
import com.kt.domain.user.User;
import com.kt.dto.review.ReviewRequest;
import com.kt.dto.review.ReviewResponse;
import com.kt.repository.review.ReviewRepository;
import com.kt.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;

	@Transactional
	public ReviewResponse createReview(Long userId, ReviewRequest.Create request) {
		if (reviewRepository.existsByUserIdAndProductIdAndDeletedFalse(userId, request.productId())) {
			throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
		}

		Review review = Review.create(
			userId,
			request.productId(),
			request.rating(),
			request.content(),
			request.reviewImageUrl()
		);

		Review savedReview = reviewRepository.save(review);

		return ReviewResponse.from(savedReview, getUserName(userId));
	}

	public Page<ReviewResponse> getReviewListByProduct(Long productId, Pageable pageable) {
		return reviewRepository.findByProductIdAndDeletedFalse(productId, pageable)
			.map(review -> {
				String userName = getUserName(review.getUserId());
				return ReviewResponse.from(review, userName);
			});
	}

	public Page<ReviewResponse> getAllReviews(Pageable pageable) {
		return reviewRepository.findAllByDeletedFalse(pageable)
			.map(review -> {
				String userName = getUserName(review.getUserId());
				return ReviewResponse.from(review, userName);
			});
	}

	public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest.Update request) {
		Review review = findReviewById(reviewId);

		validateWriter(userId, review);

		review.update(request.rating(), request.content(), request.reviewImageUrl());
		return ReviewResponse.from(review, getUserName(userId));
	}

	public void deleteReview(Long userId, Long reviewId) {
		Review review = findReviewById(reviewId);
		validateWriter(userId, review);

		review.delete(userId);
	}

	public void deleteReviewByAdmin(Long adminId, Long reviewId) {
		Review review = findReviewById(reviewId);

		review.delete(adminId);
	}

    public Page<ReviewResponse> getReviewsByUser (Long userId, Pageable pageable) {
        return reviewRepository.findByUserIdAndDeletedFalse(userId, pageable)
                .map(review -> {
									String userName = getUserName(userId);
									return ReviewResponse.from(review, userName);
								});
    }

	private Review findReviewById(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT));

		if (review.isDeleted()) {
			throw new CustomException(ErrorCode.COMMON_INVALID_ARGUMENT);
		}

		return review;
	}

	private String getUserName(Long userId) {
		return userRepository.findById(userId)
			.map(User::getName)
			.orElse("알수없음");
	}

	private void validateWriter(Long userId, Review review) {
		Preconditions.validate(
			review.isWriter(userId),
			ErrorCode.PERMISSION_DENIED
		);
	}
}