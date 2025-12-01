package com.kt.controller.review;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.review.ReviewRequest;
import com.kt.dto.review.ReviewResponse;
import com.kt.security.AuthUser;
import com.kt.service.review.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping
	public ApiResponseEntity<ReviewResponse> createReview(
		@AuthenticationPrincipal AuthUser authUser,
		@Valid @RequestBody ReviewRequest.Create request
	) {
		var response = reviewService.createReview(authUser.id(), request);
		return ApiResponseEntity.created(response);
	}

	@GetMapping
	public ApiResponseEntity<List<ReviewResponse>> getReviewListByProduct(
		@RequestParam Long productId,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponseEntity.pageOf(reviewService.getReviewListByProduct(productId, pageable));
	}

	@PutMapping("/{reviewId}")
	public ApiResponseEntity<ReviewResponse> updateReview(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewRequest.Update request
	) {
		var response = reviewService.updateReview(authUser.id(), reviewId, request);
		return ApiResponseEntity.success(response);
	}

	@DeleteMapping("/{reviewId}")
	public ApiResponseEntity<Void> deleteReview(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long reviewId
	) {
		reviewService.deleteReview(authUser.id(), reviewId);
		return ApiResponseEntity.empty();
	}
}