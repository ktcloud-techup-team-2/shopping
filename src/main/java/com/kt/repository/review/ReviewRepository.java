package com.kt.repository.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.review.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findByProductIdAndDeletedFalse(Long productId, Pageable pageable);

	Page<Review> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

	Page<Review> findAllByDeletedFalse(Pageable pageable);

	boolean existsByUserIdAndProductIdAndDeletedFalse(Long userId, Long productId);

}
