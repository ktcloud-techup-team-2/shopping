package com.kt.repository.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.review.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findByProductId(Long productId, Pageable pageable);

	Page<Review> findByUserId(Long userId, Pageable pageable);

}
