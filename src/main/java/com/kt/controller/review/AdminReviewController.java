package com.kt.controller.review;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.review.ReviewResponse;
import com.kt.service.review.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {
	private final ReviewService reviewService;

	@GetMapping
	public ApiResponseEntity<List<ReviewResponse>> getAllReviews(
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponseEntity.pageOf(reviewService.getAllReviews(pageable));
	}

	@DeleteMapping("/{reviewId}")
	public ApiResponseEntity<Void> deleteReviewByAdmin(
		@PathVariable Long reviewId
	) {
		reviewService.deleteReviewByAdmin(reviewId);
		return ApiResponseEntity.empty();
	}
}